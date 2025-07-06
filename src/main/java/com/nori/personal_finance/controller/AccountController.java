package com.nori.personal_finance.controller;

import com.nori.personal_finance.model.Account;
import com.nori.personal_finance.model.AccountType;
import com.nori.personal_finance.service.AccountService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  // Serves the full page for mobile
  @GetMapping("/new")
  public String showNewAccountForm(Model model) {
    model.addAttribute("account", new Account());
    model.addAttribute("accountTypes", AccountType.values());
    model.addAttribute("contentFragment", "user/account-form");
    return "layout";
  }

  // Serves the form fragment for the desktop modal
  @GetMapping("/new-form")
  public String getNewAccountForm(Model model) {
    model.addAttribute("account", new Account());
    model.addAttribute("accountTypes", AccountType.values());
    return "user/account-form :: content";
  }

  // Processes the form submission
  @PostMapping
  public String processNewAccount(@ModelAttribute Account account, Principal principal) {
    accountService.createAccount(account, principal.getName());
    return "redirect:/dashboard";
  }
}
