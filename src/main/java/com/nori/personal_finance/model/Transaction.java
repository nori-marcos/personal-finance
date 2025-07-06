package com.nori.personal_finance.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String description;

  private BigDecimal amount;

  private LocalDate transactionDate;

  private LocalDate paymentDate;

  @Enumerated(EnumType.STRING)
  private TransactionType type;

  @Enumerated(EnumType.STRING)
  private PaymentType paymentType;

  @ManyToOne private Account account;

  @ManyToOne private CreditCard creditCard;

  @ManyToOne private Category category;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "linked_transaction_id", referencedColumnName = "id")
  @JsonManagedReference
  private Transaction linkedTransaction;

  @JoinColumn(name = "user_email", referencedColumnName = "email")
  @ManyToOne
  private User user;
}
