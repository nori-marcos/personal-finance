package com.nori.personal_finance.controller;

import com.nori.personal_finance.configuration.Mediator;
import com.nori.personal_finance.dto.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthenticationController {

  private final Mediator mediator;

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
    if (!model.containsAttribute("userRequest")) {
      model.addAttribute("userRequest", new CreateUserRequest("", ""));
    }
    model.addAttribute("contentFragment", "user/register");
    return "layout";
  }

  @PostMapping("/register/user")
  public String processUserRegistration(
      @ModelAttribute("userRequest") final CreateUserRequest formSubmission,
      final BindingResult result,
      final RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
      redirectAttributes.addFlashAttribute(
          "org.springframework.validation.BindingResult.userRequest", result);
      redirectAttributes.addFlashAttribute("userRequest", formSubmission);
      return "redirect:/register";
    }

    try {
      mediator.handle(formSubmission, String.class);
      redirectAttributes.addFlashAttribute("successMessage", "Cadastro realizado com sucesso!");
      return "redirect:/";

    } catch (final IllegalStateException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      redirectAttributes.addFlashAttribute("userRequest", formSubmission);
      return "redirect:/register";
    }
  }
}
