package com.nori.personal_finance.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;

  @Transactional
  public void createAccount(CreateAccountRequest request, String userEmail) {
    User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + userEmail));

    // 1. Create and save the new account
    Account account = new Account();
    account.setName(request.name());
    account.setInstitution(request.institution());
    account.setAccountType(request.accountType());
    account.setUser(user);
    Account savedAccount = accountRepository.save(account);

    // 2. Corrected logic for initial balance
    BigDecimal initialBalance = request.initialBalance();

    // Proceed if the balance is not null and not zero
    if (initialBalance != null && initialBalance.compareTo(BigDecimal.ZERO) != 0) {
      Transaction initialTransaction = new Transaction();
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
}