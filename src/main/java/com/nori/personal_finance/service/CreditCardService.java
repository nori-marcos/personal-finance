package com.nori.personal_finance.service;

import com.nori.personal_finance.dto.CreditCardDetailsView;
import com.nori.personal_finance.model.CreditCard;
import com.nori.personal_finance.model.Transaction;
import com.nori.personal_finance.model.TransactionType;
import com.nori.personal_finance.model.User;
import com.nori.personal_finance.repository.CreditCardRespository;
import com.nori.personal_finance.repository.TransactionRepository;
import com.nori.personal_finance.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreditCardService {

  private final CreditCardRespository creditCardRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository; // Add dependency

  public void createCreditCard(final CreditCard creditCard, final String userEmail) {
    final User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + userEmail));

    creditCard.setUser(user);
    creditCardRepository.save(creditCard);
  }

  public CreditCardDetailsView getCreditCardDetails(final Long cardId, final String userEmail) {
    final CreditCard card =
        creditCardRepository
            .findById(cardId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid card Id:" + cardId));

    if (!card.getUser().getEmail().equals(userEmail)) {
      throw new AccessDeniedException("User does not have permission to view this card");
    }

    final BigDecimal currentInvoice = calculateCurrentInvoice(card);
    final BigDecimal availableLimit = card.getLimitAmount().subtract(currentInvoice);
    final List<Transaction> transactions = transactionRepository.findByCreditCardId(cardId);

    return new CreditCardDetailsView(card, transactions, availableLimit, currentInvoice);
  }

  @Transactional
  public void deleteCreditCard(final Long cardId, final String userEmail) {
    final CreditCard card =
        creditCardRepository
            .findById(cardId)
            .orElseThrow(() -> new IllegalStateException("Card not found"));

    if (!card.getUser().getEmail().equals(userEmail)) {
      throw new AccessDeniedException("User does not have permission to delete this card");
    }

    transactionRepository.deleteAllByCreditCardId(cardId);
    creditCardRepository.deleteById(cardId);
  }

  // This logic can be shared or kept here, it calculates the current invoice amount
  private BigDecimal calculateCurrentInvoice(final CreditCard card) {
    final LocalDate today = LocalDate.now();
    final int closingDay = card.getClosingDay();
    final LocalDate lastClosingDate =
        today.getDayOfMonth() > closingDay
            ? today.withDayOfMonth(closingDay)
            : today.minusMonths(1).withDayOfMonth(closingDay);

    return transactionRepository
        .findByCreditCardIdAndTransactionDateBetween(
            card.getId(), lastClosingDate, lastClosingDate.plusMonths(1))
        .stream()
        .filter(t -> t.getType() == TransactionType.EXPENSE)
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}