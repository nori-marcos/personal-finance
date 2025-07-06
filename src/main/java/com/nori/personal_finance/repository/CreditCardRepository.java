package com.nori.personal_finance.repository;

import com.nori.personal_finance.model.CreditCard;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
  List<CreditCard> findByUserEmail(String userEmail);

  @Transactional
  void deleteAllByUserEmail(String userEmail);
}
