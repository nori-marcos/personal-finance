package com.nori.personal_finance.service;

import com.nori.personal_finance.configuration.Handler;
import com.nori.personal_finance.dto.CreateUserRequest;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CreateUserService implements Handler<CreateUserRequest, String> {
  private final UserRepository userRepository;

  public String handle(final CreateUserRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalStateException("The user has already been registered.");
    }
    final User user = new User(request.email(), request.password());
    userRepository.save(user);
    return user.getEmail();
  }
}
