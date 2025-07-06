package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.CreateTransferRequest;
import com.nori.personal_finance.model.Account;
import com.nori.personal_finance.model.Transaction;
import com.nori.personal_finance.model.TransactionType;
import com.nori.personal_finance.model.Transfer;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.repository.TransactionRepository;
import com.nori.personal_finance.repository.TransferRepository;
import com.nori.personal_finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferService {

  private final TransferRepository transferRepository;
  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;
  private final UserRepository userRepository;

  @Transactional
  public void createTransfer(final CreateTransferRequest request, final String userEmail) {
    if (request.fromAccountId().equals(request.toAccountId())) {
      throw new IllegalArgumentException("From and To accounts cannot be the same.");
    }

    final User user =
        userRepository
            .findByEmail(userEmail)
            .orElseThrow(() -> new IllegalStateException("User not found"));

    final Account fromAccount =
        accountRepository
            .findById(request.fromAccountId())
            .orElseThrow(() -> new IllegalArgumentException("From Account not found"));

    final Account toAccount =
        accountRepository
            .findById(request.toAccountId())
            .orElseThrow(() -> new IllegalArgumentException("To Account not found"));

    // 1. Create the Expense transaction from the source account
    final Transaction expenseTransaction = new Transaction();
    expenseTransaction.setDescription("Transfer to " + toAccount.getName());
    expenseTransaction.setAmount(request.amount());
    expenseTransaction.setTransactionDate(request.date());
    expenseTransaction.setType(TransactionType.EXPENSE);
    expenseTransaction.setAccount(fromAccount);
    expenseTransaction.setUser(user);
    transactionRepository.save(expenseTransaction);

    // 2. Create the Income transaction to the destination account
    final Transaction incomeTransaction = new Transaction();
    incomeTransaction.setDescription("Transfer from " + fromAccount.getName());
    incomeTransaction.setAmount(request.amount());
    incomeTransaction.setTransactionDate(request.date());
    incomeTransaction.setType(TransactionType.INCOME);
    incomeTransaction.setAccount(toAccount);
    incomeTransaction.setUser(user);
    transactionRepository.save(incomeTransaction);

    // 3. (Optional but good practice) Log the transfer itself
    final Transfer transfer = new Transfer();
    transfer.setFromAccount(fromAccount);
    transfer.setToAccount(toAccount);
    transfer.setAmount(request.amount());
    transfer.setDate(request.date());
    transfer.setDescription(request.description());
    transfer.setUser(user);
    transferRepository.save(transfer);
  }
}
