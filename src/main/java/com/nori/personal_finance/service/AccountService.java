package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.AccountDetailsView;
import com.nori.personal_finance.dto.CreateAccountRequest;
import com.nori.personal_finance.model.Account;
import com.nori.personal_finance.model.Transaction;
import com.nori.personal_finance.model.TransactionType;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.repository.TransactionRepository;
import com.nori.personal_finance.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;

  @Transactional
  public void createAccount(final CreateAccountRequest request, final String userEmail) {
    final User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + userEmail));

    // 1. Create and save the new account
    final Account account = new Account();
    account.setName(request.name());
    account.setInstitution(request.institution());
    account.setAccountType(request.accountType());
    account.setUser(user);
    final Account savedAccount = accountRepository.save(account);

    // 2. Corrected logic for initial balance
    final BigDecimal initialBalance = request.initialBalance();

    // Proceed if the balance is not null and not zero
    if (initialBalance != null && initialBalance.compareTo(BigDecimal.ZERO) != 0) {
      final Transaction initialTransaction = new Transaction();
      initialTransaction.setDescription("Saldo Inicial");
      initialTransaction.setTransactionDate(LocalDate.now());
      initialTransaction.setAccount(savedAccount);
      initialTransaction.setUser(user);

      // If the balance is positive, create an INCOME transaction
      if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
        initialTransaction.setType(TransactionType.INCOME);
        initialTransaction.setAmount(initialBalance);
      }
      // If the balance is negative, create an EXPENSE transaction
      else {
        initialTransaction.setType(TransactionType.EXPENSE);
        // Store the amount as a positive number, the "type" makes it a debt
        initialTransaction.setAmount(initialBalance.abs());
      }

      transactionRepository.save(initialTransaction);
    }
  }

  @Transactional
  public void deleteAccount(final Long accountId, final String userEmail) {
    // First, verify the account belongs to the user trying to delete it
    final Account account =
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalStateException("Account not found"));

    if (!account.getUser().getEmail().equals(userEmail)) {
      throw new AccessDeniedException("User does not have permission to delete this account");
    }

    // Delete all transactions associated with this account
    transactionRepository.deleteAllByAccountId(accountId);

    // Finally, delete the account itself
    accountRepository.deleteById(accountId);
  }

  // Get all data for the details page
  public AccountDetailsView getAccountDetails(final Long accountId, final String userEmail) {
    // Find the account and verify it belongs to the logged-in user
    final Account account =
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid account Id:" + accountId));

    if (!account.getUser().getEmail().equals(userEmail)) {
      throw new AccessDeniedException("User does not have permission to view this account");
    }

    // Fetch related transactions
    final List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

    // Calculate the balance
    final BigDecimal balance =
        transactions.stream()
            .map(
                t -> t.getType() == TransactionType.INCOME ? t.getAmount() : t.getAmount().negate())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new AccountDetailsView(account, transactions, balance);
  }

  public List<Account> findByUserEmail(String userEmail) {
    return accountRepository.findByUserEmail(userEmail);
  }
}