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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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

  public CreditCardExpenseFormView getCreditCardExpenseFormData(final String userEmail) {
    final List<CreditCard> creditCards = creditCardRepository.findByUserEmail(userEmail);
    final List<Category> categories = categoryRepository.findByUserEmail(userEmail);
    return new CreditCardExpenseFormView(creditCards, categories);
  }

  public void createCreditCardExpense(final CreateCreditCardExpenseRequest request, final String userEmail) {
    final User user =
        userRepository
            .findByEmail(userEmail)
            .orElseThrow(() -> new IllegalStateException("User not found"));

    final CreditCard card =
        creditCardRepository
            .findById(request.creditCardId())
            .orElseThrow(() -> new IllegalArgumentException("Credit Card not found"));

    final Category category =
        categoryRepository
            .findById(request.categoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));

    final Integer installments = request.numberOfInstallments();

    // Case 1: Single payment (not an installment plan)
    if (installments == null || installments <= 1) {
      final Transaction transaction = new Transaction();
      transaction.setDescription(request.description());
      transaction.setAmount(request.amount());
      transaction.setTransactionDate(request.transactionDate());
      transaction.setType(TransactionType.EXPENSE);
      transaction.setCreditCard(card);
      transaction.setCategory(category);
      transaction.setUser(user);
      transactionRepository.save(transaction);
    }
    // Case 2: Installment plan
    else {
      final BigDecimal totalAmount = request.amount();
      // Calculate the value of each installment, rounded to 2 decimal places
      final BigDecimal installmentAmount =
          totalAmount.divide(new BigDecimal(installments), 2, RoundingMode.HALF_UP);

      BigDecimal sumOfInstallments = BigDecimal.ZERO;

      for (int i = 0; i < installments; i++) {
        final Transaction transaction = new Transaction();

        // For the last installment, adjust the amount to prevent rounding errors
        if (i == installments - 1) {
          transaction.setAmount(totalAmount.subtract(sumOfInstallments));
        } else {
          transaction.setAmount(installmentAmount);
          sumOfInstallments = sumOfInstallments.add(installmentAmount);
        }

        // Set the description to include the installment number, e.g., "Phone (1/12)"
        transaction.setDescription(
            String.format("%s (%d/%d)", request.description(), i + 1, installments));
        // Set the date for each transaction in the upcoming months
        transaction.setTransactionDate(request.transactionDate().plusMonths(i));

        transaction.setType(TransactionType.EXPENSE);
        transaction.setCreditCard(card);
        transaction.setCategory(category);
        transaction.setUser(user);

        transactionRepository.save(transaction);
      }
    }
  }

  @Transactional
  public void createCardPayment(final CreateCardPaymentRequest request, final String userEmail) {
    final User user =
        userRepository
            .findByEmail(userEmail)
            .orElseThrow(() -> new IllegalStateException("User not found"));

    final Account fromAccount =
        accountRepository
            .findById(request.fromAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Source account not found"));

    final CreditCard card =
        creditCardRepository
            .findById(request.creditCardId())
            .orElseThrow(() -> new IllegalArgumentException("Credit card not found"));

    // 1. Create the Expense from the bank account
    final Transaction expense = new Transaction();
    expense.setDescription("Pagamento da fatura: " + card.getName());
    expense.setAmount(request.amount());
    expense.setTransactionDate(request.date());
    expense.setType(TransactionType.EXPENSE);
    expense.setAccount(fromAccount);
    expense.setUser(user);
    transactionRepository.save(expense);

    // 2. Create an "Income" transaction for the credit card to balance it out
    final Transaction payment = new Transaction();
    payment.setDescription("Pagamento recebido da conta: " + fromAccount.getName());
    payment.setAmount(request.amount());
    payment.setTransactionDate(request.date());
    payment.setType(TransactionType.INCOME);
    payment.setCreditCard(card); // Link this transaction to the card
    payment.setUser(user);
    transactionRepository.save(payment);
  }

  @Transactional
  public void deleteTransaction(final Long transactionId, final String userEmail) {
    // 1. Find the transaction and verify ownership
    final Transaction transactionToDelete =
        transactionRepository
            .findById(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

    if (!transactionToDelete.getUser().getEmail().equals(userEmail)) {
      throw new AccessDeniedException("User does not have permission to delete this transaction");
    }

    // 2. Check if it's an installment transaction
    final String description = transactionToDelete.getDescription();
    final Pattern pattern = Pattern.compile("^(.*) \\(\\d+/\\d+\\)$"); // Regex to find " (x/y)"
    final Matcher matcher = pattern.matcher(description);

    if (matcher.matches()) {
      // It's an installment plan. Delete all related transactions.
      final String baseDescription = matcher.group(1); // Get the part before " (x/y)"

      final List<Transaction> allUserTransactions = transactionRepository.findByUserEmail(userEmail);
      final List<Transaction> transactionsToDelete = new ArrayList<>();

      for (final Transaction t : allUserTransactions) {
        // Find all transactions that start with the same base description
        // and have an installment pattern.
        if (t.getDescription() != null
            && t.getDescription().startsWith(baseDescription)
            && pattern.matcher(t.getDescription()).matches()) {
          transactionsToDelete.add(t);
        }
      }
      transactionRepository.deleteAll(transactionsToDelete);
    } else {
      // It's a single transaction. Just delete it.
      transactionRepository.deleteById(transactionId);
    }
  }
}
