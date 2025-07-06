package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.AccountView;
import com.nori.personal_finance.dto.CreditCardView;
import com.nori.personal_finance.dto.DashboardView;
import com.nori.personal_finance.dto.UpcomingBillView;
import com.nori.personal_finance.model.*;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.repository.CreditCardRepository;
import com.nori.personal_finance.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final AccountRepository accountRepository;
  private final CreditCardRepository creditCardRepository;
  private final TransactionRepository transactionRepository;

  public List<Account> findByUserEmail(final String userEmail) {
    return accountRepository.findByUserEmail(userEmail);
  }

  public DashboardView getDashboardData(final String userEmail) {
    // --- Initial Data Fetching ---
    final List<Account> userAccounts = accountRepository.findByUserEmail(userEmail);
    final List<CreditCard> userCreditCards = creditCardRepository.findByUserEmail(userEmail);

    final List<AccountView> accountViews =
        userAccounts.stream().map(this::mapToAccountView).toList();

    final List<CreditCardView> creditCardViews =
        userCreditCards.stream().map(this::mapToCreditCardView).toList();

    // --- METRIC 1: Dinheiro em Conta (Total in Accounts) ---
    final BigDecimal totalInAccounts =
        accountViews.stream().map(AccountView::balance).reduce(BigDecimal.ZERO, BigDecimal::add);

    // --- METRIC 2: Dívida a Quitar (Total Future Debt) ---
    final BigDecimal totalFutureDebt = calculateTotalFutureDebt(userEmail);

    // --- METRIC 3: Saldo do Mês (Monthly Balance) ---
    final BigDecimal currentMonthBalance = calculateMonthlyBalanceToDate(userEmail);

    // --- Other view data ---
    final List<UpcomingBillView> upcomingBills = getUpcomingBills(userCreditCards);

    return new DashboardView(
        totalInAccounts,
        currentMonthBalance,
        totalFutureDebt,
        accountViews,
        creditCardViews,
        upcomingBills);
  }

  private BigDecimal calculateTotalFutureDebt(final String userEmail) {
    final List<Transaction> accountExpenses =
        transactionRepository.findByUserEmailAndTypeAndTransactionDateGreaterThanEqual(
            userEmail, TransactionType.EXPENSE, LocalDate.now());
    final BigDecimal futureAccountExpenses =
        accountExpenses.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return futureAccountExpenses;
  }

  private BigDecimal calculateMonthlyBalanceToDate(final String userEmail) {
    final LocalDate today = LocalDate.now();
    final LocalDate startOfMonth = today.withDayOfMonth(1);

    final List<Transaction> transactionsThisMonth =
        transactionRepository.findByUserEmailAndTransactionDateBetween(
            userEmail, startOfMonth, today);

    final BigDecimal incomeThisMonth =
        transactionsThisMonth.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    final BigDecimal expenseThisMonth =
        transactionsThisMonth.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return incomeThisMonth.subtract(expenseThisMonth);
  }


  private List<UpcomingBillView> getUpcomingBills(final List<CreditCard> userCreditCards) {
    final List<UpcomingBillView> upcomingBills = new ArrayList<>();
    final LocalDate today = LocalDate.now();

    for (final CreditCard card : userCreditCards) {
      final BigDecimal invoiceAmount = calculateCurrentInvoice(card);
      if (invoiceAmount.compareTo(BigDecimal.ZERO) > 0) {
        LocalDate dueDate;
        if (today.getDayOfMonth() > card.getClosingDay()) {
          dueDate = today.plusMonths(1).withDayOfMonth(card.getDueDay());
        } else {
          dueDate = today.withDayOfMonth(card.getDueDay());
          if (dueDate.isBefore(today)) {
            dueDate = dueDate.plusMonths(1);
          }
        }
        final long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
        if (daysUntilDue >= 0 && daysUntilDue <= 30) {
          upcomingBills.add(
              new UpcomingBillView(card.getName(), invoiceAmount, dueDate, daysUntilDue));
        }
      }
    }
    return upcomingBills;
  }

  private BigDecimal calculateAccountBalance(final Long accountId) {
    final List<Transaction> transactions =
        transactionRepository.findByAccountIdAndTransactionDateLessThanEqual(
            accountId, LocalDate.now());
    return transactions.stream()
        .map(t -> t.getType() == TransactionType.INCOME ? t.getAmount() : t.getAmount().negate())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private AccountView mapToAccountView(final Account account) {
    final BigDecimal balance = calculateAccountBalance(account.getId());
    return new AccountView(
        account.getId(),
        account.getName(),
        account.getInstitution(),
        balance,
        account.getAccountType());
  }

  private CreditCardView mapToCreditCardView(final CreditCard card) {
    final BigDecimal currentInvoice = calculateCurrentInvoice(card);
    final BigDecimal availableLimit = card.getLimitAmount().subtract(currentInvoice);
    return new CreditCardView(card.getId(), card.getName(), availableLimit, currentInvoice);
  }

  // --- REFACTORED AND CLEANER METHOD ---
  private BigDecimal calculateCurrentInvoice(final CreditCard card) {
    final LocalDate today = LocalDate.now();
    final int closingDay = card.getClosingDay();
    final LocalDate lastClosingDate =
        today.getDayOfMonth() > closingDay
            ? today.withDayOfMonth(closingDay)
            : today.minusMonths(1).withDayOfMonth(closingDay);

    final List<Transaction> transactionsInPeriod =
        transactionRepository.findByCreditCardIdAndTransactionDateBetween(
            card.getId(), lastClosingDate, lastClosingDate.plusMonths(1));

    // Step 1: Sum all expenses in the period
    final BigDecimal totalExpenses =
        transactionsInPeriod.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Step 2: Sum all incomes (payments) in the period
    final BigDecimal totalIncomes =
        transactionsInPeriod.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Step 3: The invoice is expenses - incomes
    return totalExpenses.subtract(totalIncomes);
  }
}