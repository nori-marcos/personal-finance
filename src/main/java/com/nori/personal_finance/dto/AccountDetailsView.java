package com.nori.personal_finance.dto;

import com.nori.personal_finance.model.Account;
import com.nori.personal_finance.model.Transaction;
import java.math.BigDecimal;
import java.util.List;

public record AccountDetailsView(
    Account account, List<Transaction> transactions, BigDecimal balance) {}
