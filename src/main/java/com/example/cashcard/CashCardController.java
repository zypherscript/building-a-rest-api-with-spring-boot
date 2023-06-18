package com.example.cashcard;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/cashcards") //plural
public class CashCardController {

  //controller-repository architecture
  //layered architecture
  private final CashCardRepository cashCardRepository;

  public CashCardController(CashCardRepository cashCardRepository) {
    this.cashCardRepository = cashCardRepository;
  }

  @GetMapping("/{requestedId}") //get for fetch some
  public ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
    var cashCardOptional = Optional.ofNullable(
        cashCardRepository.findByIdAndOwner(requestedId, principal.getName()));
    return cashCardOptional.map(ResponseEntity::ok) //ok vs not found
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping //post for create
  private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest,
      UriComponentsBuilder ucb, Principal principal) {
    var cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(),
        principal.getName());
    var savedCashCard = cashCardRepository.save(cashCardWithOwner);
    var locationOfNewCashCard = ucb
        .path("cashcards/{id}")
        .buildAndExpand(savedCashCard.id())
        .toUri();
    return ResponseEntity.created(locationOfNewCashCard).build(); //create for creation
  }

  @GetMapping //get for getting all
  public ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
    var page = cashCardRepository.findByOwner(principal.getName(),
        PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount")) //default
        ));
    return ResponseEntity.ok(page.getContent());
  }
}