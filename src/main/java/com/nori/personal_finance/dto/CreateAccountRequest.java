package com.nori.personal_finance.dto;

import com.nori.personal_finance.model.AccountType;
import java.math.BigDecimal;

public record CreateAccountRequest(
    String name,
    String institution,
    AccountType accountType,
    BigDecimal initialBalance) {}