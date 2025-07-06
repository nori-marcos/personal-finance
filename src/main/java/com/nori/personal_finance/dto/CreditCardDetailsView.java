package com.nori.personal_finance.dto;

import com.nori.personal_finance.model.CreditCard;
import com.nori.personal_finance.model.Transaction;
import java.util.List;

public record CreditCardDetailsView(
    CreditCard card,
    List<Transaction> transactions,
    CardBalanceInfo balanceInfo) {}