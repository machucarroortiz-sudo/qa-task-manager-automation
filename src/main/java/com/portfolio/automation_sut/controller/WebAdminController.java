package com.portfolio.automation_sut.controller;

import com.portfolio.automation_sut.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class WebAdminController {
    private final TaskService taskService;

    public WebAdminController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("tasks", taskService.listVisibleTasks());
        return "admin/dashboard";
    }
}
