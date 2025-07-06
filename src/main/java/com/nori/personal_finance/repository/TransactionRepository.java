package com.nori.personal_finance.repository;

import com.nori.personal_finance.model.Transaction;
import jakarta.transaction.Transactional;
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

  List<Transaction> findByUserEmailAndTransactionDateBetween(
      String userEmail, LocalDate startDate, LocalDate endDate);

  List<Transaction> findByCreditCardIdAndTransactionDateBetween(
      Long creditCardId, LocalDate start, LocalDate end);

  @Transactional
  void deleteAllByAccountId(Long accountId);

  @Transactional
  void deleteAllByCreditCardId(Long creditCardId);

  List<Transaction> findByAccountIdAndTransactionDateLessThanEqual(Long accountId, LocalDate date);

}
