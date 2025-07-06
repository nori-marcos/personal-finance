package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.AccountView;
import com.nori.personal_finance.dto.CreditCardView;
import com.nori.personal_finance.dto.DashboardView;
import com.nori.personal_finance.dto.UpcomingBillView;
import com.nori.personal_finance.model.Account;
import com.nori.personal_finance.model.CreditCard;
import com.nori.personal_finance.model.Transaction;
import com.nori.personal_finance.model.TransactionType;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.repository.CreditCardRespository;
import com.nori.personal_finance.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final AccountRepository accountRepository;
  private final CreditCardRespository creditCardRepository;
  private final TransactionRepository transactionRepository;

  public DashboardView getDashboardData(final String userEmail) {
    final List<Account> userAccounts = accountRepository.findByUserEmail(userEmail);
    final List<CreditCard> userCreditCards = creditCardRepository.findByUserEmail(userEmail);

    final List<AccountView> accountViews =
        userAccounts.stream().map(this::mapToAccountView).collect(Collectors.toList());
    final List<CreditCardView> creditCardViews =
        userCreditCards.stream().map(this::mapToCreditCardView).collect(Collectors.toList());

    final BigDecimal totalBalance =
        accountViews.stream().map(AccountView::balance).reduce(BigDecimal.ZERO, BigDecimal::add);
    final BigDecimal totalInvoice =
        creditCardViews.stream()
            .map(CreditCardView::currentInvoice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    final List<UpcomingBillView> upcomingBills = new ArrayList<>();
    final LocalDate today = LocalDate.now();
    for (final CreditCard card : userCreditCards) {
      final BigDecimal invoiceAmount = calculateCurrentInvoice(card);
      if (invoiceAmount.compareTo(BigDecimal.ZERO) > 0) {
        final LocalDate dueDate =
            today.getDayOfMonth() <= card.getClosingDay()
                ? today.withDayOfMonth(card.getDueDay())
                : today.plusMonths(1).withDayOfMonth(card.getDueDay());

        final long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);

        if (daysUntilDue >= 0 && daysUntilDue <= 30) {
          upcomingBills.add(
              new UpcomingBillView(card.getName(), invoiceAmount, dueDate, daysUntilDue));
        }
      }
    }

    return new DashboardView(
        totalBalance, accountViews, totalInvoice, creditCardViews, upcomingBills);
  }

  private BigDecimal calculateAccountBalance(final Long accountId) {
    final List<Transaction> transactions =
        transactionRepository.findByAccountIdAndTransactionDateLessThanEqual(
            accountId, LocalDate.now());
    return transactions.stream()
        .map(t -> t.getType() == TransactionType.INCOME ? t.getAmount() : t.getAmount().negate())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  // This was the missing helper method
  private AccountView mapToAccountView(final Account account) {
    final BigDecimal balance = calculateAccountBalance(account.getId());
    return new AccountView(
        account.getId(),
        account.getName(),
        account.getInstitution(),
        balance,
        account.getAccountType());
  }

  // This helper method was also likely missing
  private CreditCardView mapToCreditCardView(final CreditCard card) {
    final BigDecimal currentInvoice = calculateCurrentInvoice(card);
    final BigDecimal availableLimit = card.getLimitAmount().subtract(currentInvoice);
    return new CreditCardView(card.getId(), card.getName(), availableLimit, currentInvoice);
  }

  private BigDecimal calculateCurrentInvoice(final CreditCard card) {
    final LocalDate today = LocalDate.now();
    final int closingDay = card.getClosingDay();
    final LocalDate lastClosingDate =
        today.getDayOfMonth() > closingDay
            ? today.withDayOfMonth(closingDay)
            : today.minusMonths(1).withDayOfMonth(closingDay);

    final List<Transaction> expenses =
        transactionRepository.findByCreditCardIdAndTransactionDateBetween(
            card.getId(), lastClosingDate, lastClosingDate.plusMonths(1));

    return expenses.stream()
        .map(t -> t.getType() == TransactionType.EXPENSE ? t.getAmount() : t.getAmount().negate())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}