package com.nori.personal_finance.dto;

import com.nori.personal_finance.model.Account;
import com.nori.personal_finance.model.Category;
import java.util.List;

public record TransactionFormView(List<Account> accounts, List<Category> categories) {}
