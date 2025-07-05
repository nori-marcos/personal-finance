package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.AccountView;
import com.nori.personal_finance.dto.CreditCardView;
import com.nori.personal_finance.dto.DashboardView;
import com.nori.personal_finance.model.Account;
import com.nori.personal_finance.model.CreditCard;
import com.nori.personal_finance.model.Transaction;
import com.nori.personal_finance.model.TransactionType;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.repository.CreditCardRespository;
import com.nori.personal_finance.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    return new DashboardView(totalBalance, accountViews, totalInvoice, creditCardViews);
  }

  private AccountView mapToAccountView(final Account account) {
    final BigDecimal balance = calculateAccountBalance(account.getId());
    return new AccountView(account.getName(), account.getInstitution(), balance);
  }

  private CreditCardView mapToCreditCardView(final CreditCard card) {
    final BigDecimal currentInvoice = calculateCurrentInvoice(card);
    final BigDecimal availableLimit = card.getLimitAmount().subtract(currentInvoice);
    return new CreditCardView(card.getName(), availableLimit, currentInvoice);
  }

  private BigDecimal calculateAccountBalance(final Long accountId) {
    final List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
    return transactions.stream()
        .map(t -> t.getType() == TransactionType.INCOME ? t.getAmount() : t.getAmount().negate())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal calculateCurrentInvoice(final CreditCard card) {
    final LocalDate today = LocalDate.now();
    final int closingDay = card.getClosingDay();

    LocalDate lastClosingDate;

    if (today.getDayOfMonth() > closingDay) {
      lastClosingDate = today.withDayOfMonth(closingDay);
    } else {
      lastClosingDate = today.minusMonths(1).withDayOfMonth(closingDay);
    }

    final LocalDate periodStart = lastClosingDate;
    final LocalDate periodEnd = lastClosingDate.plusMonths(1);

    final List<Transaction> cardTransactions =
        transactionRepository.findByCreditCardIdAndTransactionDateBetween(
            card.getId(), periodStart, periodEnd);

    return cardTransactions.stream()
        .filter(t -> t.getType() == TransactionType.EXPENSE)
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
