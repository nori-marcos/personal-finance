package com.nori.personal_finance.repository;

import com.nori.personal_finance.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
  List<Account> findByUserId(UUID userId);

}
