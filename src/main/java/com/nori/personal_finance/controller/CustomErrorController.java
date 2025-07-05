package com.nori.personal_finance.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

  @RequestMapping("/error")
  public String handleError(final HttpServletRequest request, final Model model) {
    final Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    String errorMsg = "";
    Integer statusCode = null;

    if (status != null) {
      statusCode = Integer.valueOf(status.toString());

      if (statusCode == HttpStatus.NOT_FOUND.value()) {
        errorMsg = "A página que você está procurando não foi encontrada.";
      } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
        errorMsg = "Você não tem permissão para acessar esta página.";
        model.addAttribute("isForbiddenError", true);
      } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
        errorMsg = "Ocorreu um erro inesperado em nosso servidor. Por favor, tente novamente mais tarde.";
      } else {
        errorMsg = "Ocorreu um erro inesperado.";
      }
    }

    model.addAttribute("statusCode", statusCode);
    model.addAttribute("errorMessage", errorMsg);
    model.addAttribute("contentFragment", "custom-error");
    return "layout";
  }
}