package com.nori.personal_finance.dto;

import java.math.BigDecimal;

public record AccountView(String name, String institution, BigDecimal balance) {}
