package com.nori.personal_finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCardPaymentRequest(
    Long fromAccountId, Long creditCardId, BigDecimal amount, LocalDate date) {}
