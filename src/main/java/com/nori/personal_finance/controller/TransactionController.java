package com.nori.personal_finance.controller;

import com.nori.personal_finance.dto.MonthlyTransactionsView;
import com.nori.personal_finance.dto.TransactionFormView;
import com.nori.personal_finance.model.Transaction;
import com.nori.personal_finance.model.TransactionType;
import com.nori.personal_finance.service.TransactionService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService transactionService;

  @GetMapping
  public String transactionsPage(
      final Model model,
      final Principal principal,
      @RequestParam(required = false) final Integer year,
      @RequestParam(required = false) final Integer month) {

    final MonthlyTransactionsView viewData =
        transactionService.getMonthlyTransactions(principal.getName(), year, month);

    model.addAttribute("transactions", viewData.transactions());
    model.addAttribute("currentDate", viewData.currentDate());
    model.addAttribute("previousDate", viewData.previousDate());
    model.addAttribute("nextDate", viewData.nextDate());
    model.addAttribute("contentFragment", "user/transactions");
    return "layout";
  }

  @GetMapping("/new")
  public String showNewTransactionForm(
      @RequestParam("type") final String type, final Model model, final Principal principal) {
    final TransactionFormView formData = transactionService.getTransactionFormData(principal.getName());

    model.addAttribute("transactionType", TransactionType.valueOf(type.toUpperCase()));
    model.addAttribute("accounts", formData.accounts());
    model.addAttribute("categories", formData.categories());
    model.addAttribute("transaction", new Transaction());
    model.addAttribute("contentFragment", "user/add-transaction-form");
    return "layout";
  }

  @GetMapping("/new-form")
  public String getNewTransactionForm(final Model model, final Principal principal) {
    final TransactionFormView formData = transactionService.getTransactionFormData(principal.getName());

    model.addAttribute("transaction", new Transaction());
    model.addAttribute("accounts", formData.accounts());
    model.addAttribute("categories", formData.categories());
    return "user/transaction-form-fields :: form";
  }

  @PostMapping
  public String processNewTransaction(
      @ModelAttribute final Transaction transaction, final Principal principal) {
    transactionService.createTransaction(transaction, principal.getName());
    return "redirect:/transactions";
  }
}
