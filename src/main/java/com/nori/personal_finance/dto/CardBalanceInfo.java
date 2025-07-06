package com.nori.personal_finance.dto;

import java.math.BigDecimal;

public record CardBalanceInfo(
    BigDecimal invoice, BigDecimal creditBalance, BigDecimal availableLimit) {}
