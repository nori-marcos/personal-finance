package com.nori.personal_finance.controller;

import com.nori.personal_finance.dto.DashboardView;
import com.nori.personal_finance.service.DashboardService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping
  public String dashboardPage(final Model model, final Principal principal) {
    final String userEmail = principal.getName();
    final DashboardView dashboardData = dashboardService.getDashboardData(userEmail);

    model.addAttribute("dashboardData", dashboardData);
    model.addAttribute("contentFragment", "user/dashboard");
    return "layout";
  }
}
