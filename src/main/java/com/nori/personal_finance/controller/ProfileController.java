package com.nori.personal_finance.controller;

import com.nori.personal_finance.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/profile")
public class ProfileController {
  private final AuthenticationService authenticationService;

  @GetMapping
  public String profilePage(final Model model) {
    model.addAttribute("contentFragment", "user/profile");
    return "layout";
  }

  @PostMapping("/delete")
  public String deleteAccount(final Principal principal, final HttpServletRequest request) {
    authenticationService.deleteUserAndAllData(principal.getName());
    SecurityContextHolder.clearContext();
    request.getSession().invalidate();
    return "redirect:/login?deleted=true";
  }
}
