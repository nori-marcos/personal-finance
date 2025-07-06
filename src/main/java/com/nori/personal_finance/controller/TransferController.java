package com.nori.personal_finance.controller;

import com.nori.personal_finance.dto.CreateTransferRequest;
import com.nori.personal_finance.repository.AccountRepository;
import com.nori.personal_finance.service.TransferService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

  private final TransferService transferService;
  private final AccountRepository accountRepository;

  @GetMapping("/new")
  public String showNewTransferForm(final Model model, final Principal principal) {
    model.addAttribute("accounts", accountRepository.findByUserEmail(principal.getName()));
    model.addAttribute("transferRequest", new CreateTransferRequest(null, null, null, null, null));
    model.addAttribute("contentFragment", "user/transfer-form");
    return "layout";
  }

  @PostMapping
  public String processNewTransfer(
      @ModelAttribute final CreateTransferRequest request, final Principal principal) {
    transferService.createTransfer(request, principal.getName());
    return "redirect:/transactions";
  }
}
