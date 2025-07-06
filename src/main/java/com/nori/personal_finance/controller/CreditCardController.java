package com.nori.personal_finance.controller;

import com.nori.personal_finance.dto.CreditCardDetailsView;
import com.nori.personal_finance.model.CreditCard;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.service.CreditCardService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CreditCardController {

  private final CreditCardService creditCardService;
  private final AccountRepository accountRepository;

  @GetMapping("/{id}")
  public String showCardDetails(@PathVariable("id") final Long id, final Model model, final Principal principal) {
    final CreditCardDetailsView viewData = creditCardService.getCreditCardDetails(id, principal.getName());
    model.addAttribute("viewData", viewData);
    model.addAttribute("contentFragment", "user/credit-card-details");
    return "layout";
  }

  @PostMapping("/{id}/delete")
  public String deleteCard(@PathVariable("id") final Long id, final Principal principal) {
    creditCardService.deleteCreditCard(id, principal.getName());
    return "redirect:/dashboard";
  }

  @GetMapping("/new")
  public String showNewCardForm(final Model model, final Principal principal) {
    model.addAttribute("creditCard", new CreditCard());
    model.addAttribute("accounts", accountRepository.findByUserEmail(principal.getName()));
    model.addAttribute("contentFragment", "user/credit-card-form");
    return "layout";
  }

  @GetMapping("/new-form")
  public String getNewCardForm(final Model model, final Principal principal) {
    model.addAttribute("creditCard", new CreditCard());
    model.addAttribute("accounts", accountRepository.findByUserEmail(principal.getName()));
    return "user/credit-card-form :: content";
  }

  @PostMapping
  public String processNewCard(@ModelAttribute final CreditCard creditCard, final Principal principal) {
    creditCardService.createCreditCard(creditCard, principal.getName());
    return "redirect:/dashboard";
  }
}