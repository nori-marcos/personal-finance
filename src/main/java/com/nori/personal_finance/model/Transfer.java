package com.nori.personal_finance.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transfers")
public class Transfer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private BigDecimal amount;

  private LocalDate date;

  private String description;

  @ManyToOne
  @JoinColumn(name = "from_account_id")
  private Account fromAccount;

  @ManyToOne
  @JoinColumn(name = "to_account_id")
  private Account toAccount;

  @ManyToOne
  @JoinColumn(name = "user_email", referencedColumnName = "email")
  private User user;
}
