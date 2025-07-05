package com.nori.personal_finance.dto;

import java.math.BigDecimal;

public record CreditCardView(String name, BigDecimal availableLimit, BigDecimal currentInvoice) {}
