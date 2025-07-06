package com.nori.personal_finance.repository;

import com.nori.personal_finance.model.Account;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
  List<Account> findByUserEmail(String userEmail);

  @Transactional
  void deleteAllByUserEmail(String userEmail);
}
