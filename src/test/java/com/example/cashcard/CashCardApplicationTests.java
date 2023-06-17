package com.example.cashcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.jayway.jsonpath.JsonPath;
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
    var response = restTemplate.getForEntity("/cashcards/99", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    var documentContext = JsonPath.parse(response.getBody());
    Number id = documentContext.read("$.id");
    assertThat(id).isEqualTo(99);

    Double amount = documentContext.read("$.amount");
    assertThat(amount).isEqualTo(123.45);
  }

  @Test
  void shouldNotReturnACashCardWithAnUnknownId() {
    var response = restTemplate.getForEntity("/cashcards/1000", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isBlank();
  }

  @Test
    //@DirtiesContext
  void shouldCreateANewCashCard() {
    var newCashCard = new CashCard(null, 250.00);
    var createResponse = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    var locationOfNewCashCard = createResponse.getHeaders().getLocation();
    var getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    var documentContext = JsonPath.parse(getResponse.getBody());
    Number id = documentContext.read("$.id");
    Double amount = documentContext.read("$.amount");

    assertThat(id).isNotNull();
    assertThat(amount).isEqualTo(250.00);
  }
}
