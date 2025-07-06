package com.nori.personal_finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCreditCardExpenseRequest(
    BigDecimal totalOrInstallmentAmount,
    String description,
    LocalDate transactionDate,
    Long categoryId,
    Long creditCardId,
    Integer numberOfInstallments) {}
