package com.nori.personal_finance.controller;

import com.nori.personal_finance.dto.CategoryRequest;
import com.nori.personal_finance.model.Category;
import com.nori.personal_finance.service.CategoryService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  public String listCategories(final Model model, final Principal principal) {
    model.addAttribute("categories", categoryService.findByUserEmail(principal.getName()));
    model.addAttribute("contentFragment", "user/category-list");
    return "layout";
  }

  @GetMapping("/new")
  public String showCreateForm(final Model model) {
    model.addAttribute("categoryRequest", new CategoryRequest("", "#000000"));
    model.addAttribute("contentFragment", "user/category-form");
    return "layout";
  }

  @PostMapping("/new")
  public String createCategory(@ModelAttribute final CategoryRequest request, final Principal principal) {
    categoryService.createCategory(request, principal.getName());
    return "redirect:/categories";
  }

  @GetMapping("/{id}/edit")
  public String showEditForm(@PathVariable("id") final Long id, final Model model, final Principal principal) {
    final Category category = categoryService.findByIdAndUser(id, principal.getName());
    model.addAttribute("category", category);
    model.addAttribute(
        "categoryRequest", new CategoryRequest(category.getName(), category.getColor()));
    model.addAttribute("contentFragment", "user/category-form");
    return "layout";
  }

  @PostMapping("/{id}/edit")
  public String updateCategory(
      @PathVariable("id") final Long id, @ModelAttribute final CategoryRequest request, final Principal principal) {
    categoryService.updateCategory(id, request, principal.getName());
    return "redirect:/categories";
  }

  @PostMapping("/{id}/delete")
  public String deleteCategory(@PathVariable("id") final Long id, final Principal principal) {
    categoryService.deleteCategory(id, principal.getName());
    return "redirect:/categories";
  }
}
