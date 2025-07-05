package com.nori.personal_finance.controller;

import com.nori.personal_finance.dto.CreateUserRequest;
import com.nori.personal_finance.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
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

  private final AuthenticationService authenticationService;

  @GetMapping("/login")
  public String userLoginPage(final Model model) {
    return "authentication/login";
  }

  @GetMapping("/register")
  public String userRegisterPage(final Model model) {
    if (!model.containsAttribute("userRequest")) {
      model.addAttribute("userRequest", new CreateUserRequest("", ""));
    }
    model.addAttribute("contentFragment", "authentication/register");
    return "authentication/register";
  }

  @PostMapping("/register")
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
      authenticationService.createUser(formSubmission);
      redirectAttributes.addFlashAttribute("successMessage", "Cadastro realizado com sucesso!");
      return "redirect:/login";

    } catch (final IllegalStateException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      redirectAttributes.addFlashAttribute("userRequest", formSubmission);
      return "redirect:/register";
    }
  }
}
