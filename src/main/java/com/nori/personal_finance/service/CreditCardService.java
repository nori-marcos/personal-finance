package com.nori.personal_finance.service;

import com.nori.personal_finance.model.CreditCard;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.CreditCardRespository;
import com.nori.personal_finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditCardService {

  private final CreditCardRespository creditCardRepository;
  private final UserRepository userRepository;

  public void createCreditCard(CreditCard creditCard, String userEmail) {
    User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + userEmail));

    creditCard.setUser(user);
    creditCardRepository.save(creditCard);
  }
}