package com.portfolio.automation_sut.controller;

import com.portfolio.automation_sut.dto.WebLabelForm;
import com.portfolio.automation_sut.service.LabelService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/labels")
public class WebLabelController {
    private final LabelService labelService;

    public WebLabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping
    public String labels(Model model) {
        model.addAttribute("labels", labelService.listLabels());
        model.addAttribute("label", new WebLabelForm("", "#2868c7"));
        return "admin/labels";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("label") WebLabelForm label, BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("labels", labelService.listLabels());
            return "admin/labels";
        }
        labelService.createLabel(label.toLabelRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Label created successfully");
        return "redirect:/admin/labels";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("label") WebLabelForm label,
                         BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Label name and hex color are required");
            return "redirect:/admin/labels";
        }
        labelService.updateLabel(id, label.toLabelRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Label updated successfully");
        return "redirect:/admin/labels";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        labelService.deleteLabel(id);
        redirectAttributes.addFlashAttribute("successMessage", "Label deleted successfully");
        return "redirect:/admin/labels";
    }
}
