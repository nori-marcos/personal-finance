package com.nori.personal_finance.controller;

import com.nori.personal_finance.dto.AccountDetailsView;
import com.nori.personal_finance.dto.CreateAccountRequest;
import com.nori.personal_finance.model.Account;
import com.nori.personal_finance.model.AccountType;
import com.nori.personal_finance.service.AccountService;
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
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @GetMapping("/{id}")
  public String showAccountDetails(@PathVariable("id") final Long id, final Model model, final Principal principal) {
    final AccountDetailsView viewData = accountService.getAccountDetails(id, principal.getName());

    model.addAttribute("account", viewData.account());
    model.addAttribute("transactions", viewData.transactions());
    model.addAttribute("balance", viewData.balance());
    model.addAttribute("contentFragment", "user/account-details");
    return "layout";
  }

  @GetMapping("/new")
  public String showNewAccountForm(final Model model) {
    model.addAttribute("account", new Account());
    model.addAttribute("accountTypes", AccountType.values());
    model.addAttribute("contentFragment", "user/account-form");
    return "layout";
  }

  @GetMapping("/new-form")
  public String getNewAccountForm(final Model model) {
    model.addAttribute("account", new Account());
    model.addAttribute("accountTypes", AccountType.values());
    return "user/account-form :: content";
  }

  @PostMapping
  public String processNewAccount(@ModelAttribute final CreateAccountRequest request, final Principal principal) {
    accountService.createAccount(request, principal.getName());
    return "redirect:/dashboard";
  }

  @PostMapping("/{id}/delete")
  public String deleteAccount(@PathVariable("id") final Long id, final Principal principal) {
    accountService.deleteAccount(id, principal.getName());
    return "redirect:/dashboard";
  }

  @GetMapping
  public String listAccounts(final Model model, final Principal principal) {
    model.addAttribute("accounts", accountService.findByUserEmail(principal.getName()));
    model.addAttribute("contentFragment", "user/account-list");
    return "layout";
  }
}
