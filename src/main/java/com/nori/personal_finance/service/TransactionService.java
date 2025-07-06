package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.CreateCardPaymentRequest;
import com.nori.personal_finance.dto.CreateCreditCardExpenseRequest;
import com.nori.personal_finance.dto.CreditCardExpenseFormView;
import com.nori.personal_finance.dto.MonthlyTransactionsView;
import com.nori.personal_finance.dto.TransactionFormView;
import com.nori.personal_finance.model.Account;
import com.nori.personal_finance.model.Category;
import com.nori.personal_finance.model.CreditCard;
import com.nori.personal_finance.model.Transaction;
import com.nori.personal_finance.model.TransactionType;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.repository.CategoryRepository;
import com.nori.personal_finance.repository.CreditCardRespository;
import com.nori.personal_finance.repository.TransactionRepository;
import com.nori.personal_finance.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

  private final CreditCardRespository creditCardRepository;
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

  public CreditCardExpenseFormView getCreditCardExpenseFormData(String userEmail) {
    List<CreditCard> creditCards = creditCardRepository.findByUserEmail(userEmail);
    List<Category> categories = categoryRepository.findByUserEmail(userEmail);
    return new CreditCardExpenseFormView(creditCards, categories);
  }

  public void createCreditCardExpense(CreateCreditCardExpenseRequest request, String userEmail) {
    User user =
        userRepository
            .findByEmail(userEmail)
            .orElseThrow(() -> new IllegalStateException("User not found"));

    CreditCard card =
        creditCardRepository
            .findById(request.creditCardId())
            .orElseThrow(() -> new IllegalArgumentException("Credit Card not found"));

    Category category =
        categoryRepository
            .findById(request.categoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));

    Transaction transaction = new Transaction();
    transaction.setDescription(request.description());
    transaction.setAmount(request.amount());
    transaction.setTransactionDate(request.transactionDate());
    transaction.setType(TransactionType.EXPENSE);
    transaction.setCreditCard(card); // Link to the credit card
    transaction.setCategory(category);
    transaction.setUser(user);

    transactionRepository.save(transaction);
  }

  @Transactional
  public void createCardPayment(CreateCardPaymentRequest request, String userEmail) {
    User user =
        userRepository
            .findByEmail(userEmail)
            .orElseThrow(() -> new IllegalStateException("User not found"));

    Account fromAccount =
        accountRepository
            .findById(request.fromAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Source account not found"));

    CreditCard card =
        creditCardRepository
            .findById(request.creditCardId())
            .orElseThrow(() -> new IllegalArgumentException("Credit card not found"));

    // 1. Create the Expense from the bank account
    Transaction expense = new Transaction();
    expense.setDescription("Pagamento da fatura: " + card.getName());
    expense.setAmount(request.amount());
    expense.setTransactionDate(request.date());
    expense.setType(TransactionType.EXPENSE);
    expense.setAccount(fromAccount);
    expense.setUser(user);
    transactionRepository.save(expense);

    // 2. Create an "Income" transaction for the credit card to balance it out
    Transaction payment = new Transaction();
    payment.setDescription("Pagamento recebido da conta: " + fromAccount.getName());
    payment.setAmount(request.amount());
    payment.setTransactionDate(request.date());
    payment.setType(TransactionType.INCOME);
    payment.setCreditCard(card); // Link this transaction to the card
    payment.setUser(user);
    transactionRepository.save(payment);
  }
}
