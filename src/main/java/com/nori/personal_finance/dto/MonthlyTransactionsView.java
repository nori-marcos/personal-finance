package com.nori.personal_finance.dto;

import com.nori.personal_finance.model.Transaction;
import java.time.LocalDate;
import java.util.List;

public record MonthlyTransactionsView(
    List<Transaction> transactions,
    LocalDate currentDate,
    LocalDate previousDate,
    LocalDate nextDate) {}
