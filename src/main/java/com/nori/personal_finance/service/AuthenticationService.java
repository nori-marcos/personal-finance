package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.CreateUserRequest;
import com.nori.personal_finance.model.Category;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.CategoryRepository;
import com.nori.personal_finance.repository.UserRepository;
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
        List.of("Entretenimento", "Estudo", "Transporte", "Casa", "Salário", "Compras");

    final List<Category> defaultCategories =
        defaultCategoryNames.stream()
            .map(
                name -> {
                  final Category category = new Category();
                  category.setName(name);
                  category.setUser(user);
                  return category;
                })
            .toList();

    categoryRepository.saveAll(defaultCategories);
  }
}
