package com.nori.personal_finance.controller;

import com.nori.personal_finance.dto.CreateCreditCardExpenseRequest;
import com.nori.personal_finance.dto.CreditCardExpenseFormView;
import com.nori.personal_finance.dto.MonthlyTransactionsView;
import com.nori.personal_finance.dto.TransactionFormView;
import com.nori.personal_finance.model.Transaction;
import com.nori.personal_finance.model.TransactionType;
import com.nori.personal_finance.repository.CategoryRepository;
import com.nori.personal_finance.service.TransactionService;
import com.nori.personal_finance.repository.CreditCardRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService transactionService;
  private final CreditCardRepository creditCardRepository;
  private final CategoryRepository categoryRepository;

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

  @GetMapping("/new/credit-card")
  public String showNewCreditCardExpenseForm(final Model model, final Principal principal) {
    final CreditCardExpenseFormView formData =
        transactionService.getCreditCardExpenseFormData(principal.getName());
    model.addAttribute(
        "expenseRequest", new CreateCreditCardExpenseRequest(null, null, null, null, null, null));
    model.addAttribute("creditCards", formData.creditCards());
    model.addAttribute("categories", formData.categories());
    model.addAttribute("contentFragment", "user/credit-card-expense-form");
    return "layout";
  }

  // NEW: Process the credit card expense form
  @PostMapping("/credit-card")
  public String processNewCreditCardExpense(
      @ModelAttribute final CreateCreditCardExpenseRequest request, final Principal principal) {
    transactionService.createCreditCardExpense(request, principal.getName());
    return "redirect:/transactions";
  }

  @PostMapping("/{id}/delete")
  public String deleteTransaction(@PathVariable("id") final Long id, final Principal principal) {
    transactionService.deleteTransaction(id, principal.getName());
    // Redirect back to the main transactions list
    return "redirect:/transactions";
  }

  @GetMapping("/new-form/credit-card")
  public String getNewCreditCardExpenseForm(final Model model, final Principal principal) {
    model.addAttribute("expenseRequest", new CreateCreditCardExpenseRequest(null, null, null, null, null, null));
    model.addAttribute("creditCards", creditCardRepository.findByUserEmail(principal.getName()));
    model.addAttribute("categories", categoryRepository.findByUserEmailExcludingTransferencia(principal.getName()));
    return "user/credit-card-expense-form :: content";
  }
}
