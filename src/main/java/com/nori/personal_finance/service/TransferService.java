package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.CreateTransferRequest;
import com.nori.personal_finance.model.*;
import com.nori.personal_finance.repository.*;
import java.util.List;
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
  private final CategoryRepository categoryRepository;

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

    final Category transferCategory =
        categoryRepository
            .findByNameAndUserEmail("Transferência", userEmail)
            .orElseGet(
                () -> {
                  final Category newCategory = new Category();
                  newCategory.setName("Transferência");
                  newCategory.setColor("#808080");
                  newCategory.setUser(user);
                  return categoryRepository.save(newCategory);
                });

    // Create both transactions first
    final Transaction expense = new Transaction();
    expense.setDescription("Transferência para " + toAccount.getName());
    expense.setType(TransactionType.EXPENSE);
    expense.setAccount(fromAccount);

    final Transaction income = new Transaction();
    income.setDescription("Transferência de " + fromAccount.getName());
    income.setType(TransactionType.INCOME);
    income.setAccount(toAccount);

    // Set shared properties
    List.of(expense, income)
        .forEach(
            t -> {
              t.setAmount(request.amount());
              t.setTransactionDate(request.date());
              t.setUser(user);
              t.setCategory(transferCategory);
            });

    // Link them together
    expense.setLinkedTransaction(income);
    income.setLinkedTransaction(expense);

    // Save them both
    transactionRepository.save(expense);
    transactionRepository.save(income);

    // Log the transfer event
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