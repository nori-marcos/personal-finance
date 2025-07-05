package com.nori.personal_finance.repository;

import com.nori.personal_finance.model.Transaction;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  List<Transaction> findByUserEmail(String userEmail);

  List<Transaction> findByAccountId(Long accountId);

  List<Transaction> findByCreditCardId(Long creditCardId);

  List<Transaction> findByPaymentDateBetween(LocalDate start, LocalDate end);
}
