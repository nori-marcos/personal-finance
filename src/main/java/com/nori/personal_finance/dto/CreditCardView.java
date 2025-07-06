package com.nori.personal_finance.dto;

import java.math.BigDecimal;

public record CreditCardView(
    Long id, String name, BigDecimal availableLimit, BigDecimal currentInvoice) {}
