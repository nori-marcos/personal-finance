package com.nori.personal_finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransferRequest(
    Long fromAccountId, Long toAccountId, BigDecimal amount, LocalDate date, String description) {}
