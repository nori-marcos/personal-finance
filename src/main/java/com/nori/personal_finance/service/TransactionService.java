package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.MonthlyTransactionsView;
import com.nori.personal_finance.dto.TransactionFormView;
import com.nori.personal_finance.model.Account;
import com.nori.personal_finance.model.Category;
import com.nori.personal_finance.model.Transaction;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.repository.CategoryRepository;
import com.nori.personal_finance.repository.TransactionRepository;
import com.nori.personal_finance.repository.UserRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  public MonthlyTransactionsView getMonthlyTransactions(
      final String userEmail, final Integer year, final Integer month) {
    final LocalDate currentDate =
        (year != null && month != null) ? LocalDate.of(year, month, 1) : LocalDate.now();

    final YearMonth yearMonth = YearMonth.from(currentDate);
    final LocalDate startDate = yearMonth.atDay(1);
    final LocalDate endDate = yearMonth.atEndOfMonth();

    final List<Transaction> transactions =
        transactionRepository.findByUserEmailAndTransactionDateBetween(
            userEmail, startDate, endDate);
    transactions.sort(Comparator.comparing(Transaction::getTransactionDate).reversed());

    return new MonthlyTransactionsView(
        transactions, currentDate, currentDate.minusMonths(1), currentDate.plusMonths(1));
  }

  public TransactionFormView getTransactionFormData(final String userEmail) {
    final List<Account> accounts = accountRepository.findByUserEmail(userEmail);
    final List<Category> categories = categoryRepository.findByUserEmail(userEmail);
    return new TransactionFormView(accounts, categories);
  }

  public void createTransaction(final Transaction transaction, final String userEmail) {
    final User user =
        userRepository
            .findByEmail(userEmail)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userEmail));
    transaction.setUser(user);
    transactionRepository.save(transaction);
  }
}
