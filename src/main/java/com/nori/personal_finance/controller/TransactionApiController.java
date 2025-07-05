package com.nori.personal_finance.controller;

import com.nori.personal_finance.model.Transaction;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.TransactionRepository;
import com.nori.personal_finance.repository.UserRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionApiController {

  private final TransactionRepository transactionRepository;
  private final UserRepository userRepository;

  @PostMapping
  public ResponseEntity<Transaction> createTransaction(
      @ModelAttribute final Transaction transaction, final Principal principal) {
    final User user =
        userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() -> new IllegalStateException("User not found"));

    transaction.setUser(user);

    final Transaction savedTransaction = transactionRepository.save(transaction);
    return ResponseEntity.ok(savedTransaction);
  }
}
