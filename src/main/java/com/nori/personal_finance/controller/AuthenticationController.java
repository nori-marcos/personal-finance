package com.nori.personal_finance.controller;

import com.nori.personal_finance.configuration.Handler;
import com.nori.personal_finance.dto.CreateUserRequest;
import com.nori.personal_finance.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthenticationController {

  private final Handler handler;

  @GetMapping("/")
  public String userLoginPage(final Authentication authentication, final Model model) {
    if (authentication != null && authentication.isAuthenticated()) {
      return "redirect:/user/dashboard";
    }
    model.addAttribute("contentFragment", "user/login");
    return "layout";
  }

  @GetMapping("/register")
  public String userRegisterPage(final Model model) {
    model.addAttribute("userRequest", new User());
    model.addAttribute("contentFragment", "user/register");
    return "layout";
  }

  @PostMapping("/register/user")
  public String processUserRegistration(
      @ModelAttribute("userRequest") final CreateUserRequest formSubmission,
      final RedirectAttributes redirectAttributes) {
    try {
      final CreateUserRequest safeRequest =
          new CreateUserRequest(formSubmission.getEmail(), formSubmission.getPassword());

      final String username = handler.execute(safeRequest);
      redirectAttributes.addFlashAttribute(
          "successMessage", "Cadastro realizado com sucesso! Por favor, faça o login.");
      redirectAttributes.addFlashAttribute("username", username);
      return "redirect:/";

    } catch (final IllegalStateException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      redirectAttributes.addFlashAttribute("userRequest", formSubmission);
      return "redirect:/register";
    }
  }
}
