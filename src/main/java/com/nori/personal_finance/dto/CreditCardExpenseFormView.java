package com.nori.personal_finance.dto;

import com.nori.personal_finance.model.Category;
import com.nori.personal_finance.model.CreditCard;
import java.util.List;

public record CreditCardExpenseFormView(List<CreditCard> creditCards, List<Category> categories) {}
