package com.nori.personal_finance.controller;

import com.nori.personal_finance.dto.CreateCardPaymentRequest;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.repository.CreditCardRepository;
import com.nori.personal_finance.service.TransactionService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/card-payments")
@RequiredArgsConstructor
public class CardPaymentController {

  private final TransactionService transactionService;
  private final AccountRepository accountRepository;
  private final CreditCardRepository creditCardRepository;

  @GetMapping("/new")
  public String showNewPaymentForm(final Model model, final Principal principal) {
    final String userEmail = principal.getName();
    model.addAttribute("paymentRequest", new CreateCardPaymentRequest(null, null, null, null));
    model.addAttribute("accounts", accountRepository.findByUserEmail(userEmail));
    model.addAttribute("creditCards", creditCardRepository.findByUserEmail(userEmail));
    model.addAttribute("contentFragment", "user/card-payment-form");
    return "layout";
  }

  @PostMapping
  public String processNewPayment(
      @ModelAttribute final CreateCardPaymentRequest request, final Principal principal) {
    transactionService.createCardPayment(request, principal.getName());
    return "redirect:/transactions";
  }

  @GetMapping("/new-form")
  public String getNewPaymentForm(final Model model, final Principal principal) {
    final String userEmail = principal.getName();
    model.addAttribute("paymentRequest", new CreateCardPaymentRequest(null, null, null, null));
    model.addAttribute("accounts", accountRepository.findByUserEmail(userEmail));
    model.addAttribute("creditCards", creditCardRepository.findByUserEmail(userEmail));
    return "user/card-payment-form :: content";
  }
}
