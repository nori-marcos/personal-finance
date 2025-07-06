package com.nori.personal_finance.controller;

import com.nori.personal_finance.model.CreditCard;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.service.CreditCardService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CreditCardController {

  private final CreditCardService creditCardService;
  private final AccountRepository accountRepository;

  // Serves the full page for mobile
  @GetMapping("/new")
  public String showNewCardForm(Model model, Principal principal) {
    model.addAttribute("creditCard", new CreditCard());
    model.addAttribute("accounts", accountRepository.findByUserEmail(principal.getName()));
    model.addAttribute("contentFragment", "user/credit-card-form");
    return "layout";
  }

  // Serves the form fragment for the desktop modal
  @GetMapping("/new-form")
  public String getNewCardForm(Model model, Principal principal) {
    model.addAttribute("creditCard", new CreditCard());
    model.addAttribute("accounts", accountRepository.findByUserEmail(principal.getName()));
    return "user/credit-card-form :: content"; // Changed to 'content' for consistency
  }

  // Processes the form submission
  @PostMapping
  public String processNewCard(@ModelAttribute CreditCard creditCard, Principal principal) {
    creditCardService.createCreditCard(creditCard, principal.getName());
    return "redirect:/dashboard";
  }
}