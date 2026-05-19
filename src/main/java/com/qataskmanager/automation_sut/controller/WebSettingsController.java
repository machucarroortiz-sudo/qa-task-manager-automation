package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.service.ShellModelService;
import com.qataskmanager.automation_sut.service.TestDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class WebSettingsController {
    private final TestDataService testDataService;
    private final ShellModelService shellModelService;

    public WebSettingsController(TestDataService testDataService, ShellModelService shellModelService) {
        this.testDataService = testDataService;
        this.shellModelService = shellModelService;
    }

    @GetMapping
    public String settings(Model model) {
        shellModelService.addShellAttributes(model);
        return "settings";
    }

    @PostMapping("/reset")
    public String reset(RedirectAttributes redirectAttributes) {
        var summary = testDataService.reset();
        redirectAttributes.addFlashAttribute("successMessage", "SUT data reset completed");
        redirectAttributes.addFlashAttribute("dataSummary", summary);
        return "redirect:/settings";
    }

    @PostMapping("/clear")
    public String clear(RedirectAttributes redirectAttributes) {
        try {
            var summary = testDataService.clearAllData();
            redirectAttributes.addFlashAttribute("successMessage", "All SUT data cleared successfully");
            redirectAttributes.addFlashAttribute("dataSummary", summary);
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not clear SUT data");
        }
        return "redirect:/settings";
    }

    @PostMapping("/demo")
    public String demo(RedirectAttributes redirectAttributes) {
        var summary = testDataService.loadDemoData();
        redirectAttributes.addFlashAttribute("successMessage", "Demo data loaded successfully");
        redirectAttributes.addFlashAttribute("dataSummary", summary);
        return "redirect:/settings";
    }

    @PostMapping("/import")
    public String importData(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            var summary = testDataService.importData(file);
            redirectAttributes.addFlashAttribute("successMessage", "Test data imported successfully");
            redirectAttributes.addFlashAttribute("dataSummary", summary);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/settings";
    }
}
