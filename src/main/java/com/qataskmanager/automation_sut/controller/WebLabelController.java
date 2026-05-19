package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.dto.WebLabelForm;
import com.qataskmanager.automation_sut.service.LabelService;
import com.qataskmanager.automation_sut.service.ShellModelService;
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
    private final ShellModelService shellModelService;

    public WebLabelController(LabelService labelService, ShellModelService shellModelService) {
        this.labelService = labelService;
        this.shellModelService = shellModelService;
    }

    @GetMapping
    public String labels(Model model) {
        addPageAttributes(model, new WebLabelForm("", "#2868c7"));
        return "admin/labels";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("label") WebLabelForm label, BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addPageAttributes(model, label);
            return "admin/labels";
        }
        labelService.createLabel(label.toLabelRequest());
        redirectAttributes.addFlashAttribute("successMessageKey", "flash.label.created");
        return "redirect:/admin/labels";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("label") WebLabelForm label,
                         BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessageKey", "flash.label.invalid");
            return "redirect:/admin/labels";
        }
        labelService.updateLabel(id, label.toLabelRequest());
        redirectAttributes.addFlashAttribute("successMessageKey", "flash.label.updated");
        return "redirect:/admin/labels";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        labelService.deleteLabel(id);
        redirectAttributes.addFlashAttribute("successMessageKey", "flash.label.deleted");
        return "redirect:/admin/labels";
    }

    private void addPageAttributes(Model model, WebLabelForm label) {
        model.addAttribute("labels", labelService.listLabels());
        model.addAttribute("label", label);
        shellModelService.addShellAttributes(model);
    }
}
