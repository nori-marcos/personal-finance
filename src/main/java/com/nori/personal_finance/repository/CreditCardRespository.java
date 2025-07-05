package com.nori.personal_finance.repository;

import com.nori.personal_finance.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardRespository extends JpaRepository<CreditCard, Long> {}
