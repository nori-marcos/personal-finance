package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.CategoryRequest;
import com.nori.personal_finance.model.Category;
import com.nori.personal_finance.model.Transaction;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.CategoryRepository;
import com.nori.personal_finance.repository.TransactionRepository;
import com.nori.personal_finance.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;

  public List<Category> findByUserEmail(final String userEmail) {
    return categoryRepository.findByUserEmail(userEmail);
  }

  public Category findByIdAndUser(final Long id, final String userEmail) {
    final Category category = categoryRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    if (!category.getUser().getEmail().equals(userEmail)) {
      throw new AccessDeniedException("User does not have permission to access this category");
    }
    return category;
  }

  public void createCategory(final CategoryRequest request, final String userEmail) {
    final User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("User not found"));

    final Category category = new Category();
    category.setName(request.name());
    category.setColor(request.color());
    category.setUser(user);
    categoryRepository.save(category);
  }

  public void updateCategory(final Long id, final CategoryRequest request, final String userEmail) {
    final Category category = findByIdAndUser(id, userEmail); // Reuse find method for security check
    category.setName(request.name());
    category.setColor(request.color());
    categoryRepository.save(category);
  }

  @Transactional
  public void deleteCategory(final Long id, final String userEmail) {
    final Category category = findByIdAndUser(id, userEmail); // Security check

    // Set the category to null for all associated transactions before deleting
    final List<Transaction> transactions = transactionRepository.findByCategoryId(id);
    if (transactions != null && !transactions.isEmpty()) {
      transactions.forEach(transaction -> transaction.setCategory(null));
      transactionRepository.saveAll(transactions);
    }

    categoryRepository.delete(category);
  }
}