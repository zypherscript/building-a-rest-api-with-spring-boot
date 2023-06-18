package com.example.cashcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {

  @Autowired
  TestRestTemplate restTemplate;

  @Test
  void shouldReturnACashCardWhenDataIsSaved() {
    var response = restTemplate.withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards/99", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    var documentContext = JsonPath.parse(response.getBody());
    Number id = documentContext.read("$.id");
    assertThat(id).isEqualTo(99);

    Double amount = documentContext.read("$.amount");
    assertThat(amount).isEqualTo(123.45);
  }

  @Test
  void shouldNotReturnACashCardWithAnUnknownId() {
    var response = restTemplate.withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards/1000", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isBlank();
  }

  @Test
    //@DirtiesContext
  void shouldCreateANewCashCard() {
    CashCard newCashCard = new CashCard(null, 250.00, null);
    var createResponse = restTemplate.withBasicAuth("sarah1", "abc123")
        .postForEntity("/cashcards", newCashCard, Void.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    var locationOfNewCashCard = createResponse.getHeaders().getLocation();
    var getResponse = restTemplate.withBasicAuth("sarah1", "abc123")
        .getForEntity(locationOfNewCashCard, String.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    var documentContext = JsonPath.parse(getResponse.getBody());
    Number id = documentContext.read("$.id");
    Double amount = documentContext.read("$.amount");

    assertThat(id).isNotNull();
    assertThat(amount).isEqualTo(250.00);
  }

  @Test
  void shouldReturnAllCashCardsWhenListIsRequested() {
    var response = restTemplate.withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    var documentContext = JsonPath.parse(response.getBody());
    int cashCardCount = documentContext.read("$.length()");
    assertThat(cashCardCount).isEqualTo(3);

    JSONArray ids = documentContext.read("$..id");
    assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

    JSONArray amounts = documentContext.read("$..amount");
    assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0, 150.00);
  }

  @Test
  void shouldReturnAPageOfCashCards() {
    var response = restTemplate.withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards?page=0&size=1", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    var documentContext = JsonPath.parse(response.getBody());
    JSONArray page = documentContext.read("$[*]");
    assertThat(page.size()).isEqualTo(1);
  }

  @Test
  void shouldReturnASortedPageOfCashCards() {
    var response = restTemplate.withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards?page=0&size=1&sort=amount,asc", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    var documentContext = JsonPath.parse(response.getBody());
    JSONArray read = documentContext.read("$[*]");
    assertThat(read.size()).isEqualTo(1);

    double amount = documentContext.read("$[0].amount");
    assertThat(amount).isEqualTo(1.00);
  }

  @Test
  void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
    var response = restTemplate.withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    var documentContext = JsonPath.parse(response.getBody());
    JSONArray page = documentContext.read("$[*]");
    assertThat(page.size()).isEqualTo(3);

    JSONArray amounts = documentContext.read("$..amount");
    assertThat(amounts).containsExactly(1.00, 123.45, 150.00);
  }

  @Test
  void shouldNotReturnACashCardWhenUsingBadCredentials() {
    var response = restTemplate.withBasicAuth("BAD-USER", "abc123")
        .getForEntity("/cashcards/99", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    response = restTemplate.withBasicAuth("sarah1", "BAD-PASSWORD")
        .getForEntity("/cashcards/99", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldRejectUsersWhoAreNotCardOwners() {
    var response = restTemplate.withBasicAuth("hank-owns-no-cards", "qrs456")
        .getForEntity("/cashcards/99", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
    var response = restTemplate.withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards/102", String.class); // kumar2's data
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
