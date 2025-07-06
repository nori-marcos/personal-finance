package com.nori.personal_finance.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardView(
    // New, clearer metric names
    BigDecimal totalInAccounts,
    BigDecimal currentMonthBalance,
    BigDecimal totalFutureDebt,

    // Existing lists for the cards below
    List<AccountView> accounts,
    List<CreditCardView> creditCards,
    List<UpcomingBillView> upcomingBills) {}