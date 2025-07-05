package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.CreateUserRequest;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuthenticationService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public String createUser(final CreateUserRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalStateException("The user has already been registered.");
    }
    final String encodedPassword = passwordEncoder.encode(request.password());
    final User user = new User(request.email(), encodedPassword);
    userRepository.save(user);
    return user.getEmail();
  }
}
