package com.nori.personal_finance.service;

import com.nori.personal_finance.model.Account;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final UserRepository userRepository;

  public void createAccount(Account account, String userEmail) {
    User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + userEmail));
    account.setUser(user);
    accountRepository.save(account);
  }
}