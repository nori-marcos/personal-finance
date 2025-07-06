package com.nori.personal_finance.repository;

import com.nori.personal_finance.model.Transfer;
import java.util.List;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
  List<Transfer> findByUserEmail(String userEmail);

  List<Transfer> findByFromAccountIdOrToAccountId(Long fromId, Long toId);

  @Transactional
  void deleteAllByUserEmail(String userEmail);
}
