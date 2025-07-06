package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.CreateUserRequest;
import com.nori.personal_finance.model.Category;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.repository.CategoryRepository;
import com.nori.personal_finance.repository.CreditCardRepository;
import com.nori.personal_finance.repository.TransactionRepository;
import com.nori.personal_finance.repository.TransferRepository;
import com.nori.personal_finance.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuthenticationService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CategoryRepository categoryRepository;
  private final TransactionRepository transactionRepository;
  private final TransferRepository transferRepository;
  private final CreditCardRepository creditCardRepository;
  private final AccountRepository accountRepository;

  public String createUser(final CreateUserRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalStateException("The user has already been registered.");
    }
    final String encodedPassword = passwordEncoder.encode(request.password());
    final User user = new User(request.email(), encodedPassword);
    userRepository.save(user);
    createDefaultCategoriesForUser(user);
    return user.getEmail();
  }

  private void createDefaultCategoriesForUser(final User user) {
    final List<String> defaultCategoryNames =
        List.of("Entretenimento", "Estudo", "Transporte", "Casa", "Salário", "Compras", "Transferência");

    final List<Category> defaultCategories =
        defaultCategoryNames.stream()
            .map(
                name -> {
                  final Category category = new Category();
                  category.setName(name);
                  category.setUser(user);
                  category.setColor("#808080");
                  return category;
                })
            .toList();

    categoryRepository.saveAll(defaultCategories);
  }

  @Transactional
  public void deleteUserAndAllData(final String userEmail) {
    if (!userRepository.existsByEmail(userEmail)) {
      throw new IllegalStateException("User not found");
    }

    // Delete in the correct order to respect foreign key constraints
    // 1. Data that depends on Accounts and Cards
    transferRepository.deleteAllByUserEmail(userEmail);
    transactionRepository.deleteAllByUserEmail(userEmail);

    // 2. Data that depends on User
    creditCardRepository.deleteAllByUserEmail(userEmail);
    accountRepository.deleteAllByUserEmail(userEmail);
    categoryRepository.deleteAllByUserEmail(userEmail);

    // 3. Finally, delete the User
    userRepository.deleteByEmail(userEmail);
  }
}
