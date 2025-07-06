package com.nori.personal_finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpcomingBillView(
    String cardName, BigDecimal amount, LocalDate dueDate, long daysUntilDue) {}
