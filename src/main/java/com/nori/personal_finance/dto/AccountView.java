package com.nori.personal_finance.dto;

import com.nori.personal_finance.model.AccountType;
import java.math.BigDecimal;

public record AccountView(
    Long id, String name, String institution, BigDecimal balance, AccountType accountType) {}
