package com.nori.personal_finance.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardView(
    BigDecimal totalBalance,
    List<AccountView> accounts,
    BigDecimal totalInvoice,
    List<CreditCardView> creditCards) {}
