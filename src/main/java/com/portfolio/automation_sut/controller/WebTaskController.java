package com.portfolio.automation_sut.controller;

import com.portfolio.automation_sut.dto.WebTaskForm;
import com.portfolio.automation_sut.model.Role;
import com.portfolio.automation_sut.model.TaskPriority;
import com.portfolio.automation_sut.model.TaskStatus;
import com.portfolio.automation_sut.service.CurrentUserService;
import com.portfolio.automation_sut.service.TaskService;
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
@RequestMapping("/tasks")
public class WebTaskController {
    private final TaskService taskService;
    private final CurrentUserService currentUserService;

    public WebTaskController(TaskService taskService, CurrentUserService currentUserService) {
        this.taskService = taskService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tasks", taskService.listVisibleTasks());
        model.addAttribute("isAdmin", currentUserService.getCurrentUser().getRole() == Role.ADMIN);
        return "tasks/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        addFormAttributes(model, new WebTaskForm("", TaskStatus.TODO, TaskPriority.MEDIUM, null));
        return "tasks/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("task") WebTaskForm task, BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, task);
            return "tasks/form";
        }
        taskService.createTask(task.toTaskRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Task created successfully");
        return "redirect:/tasks";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskService.getVisibleTask(id));
        return "tasks/details";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var task = taskService.getVisibleTask(id);
        addFormAttributes(model, new WebTaskForm(task.title(), task.status(), task.priority(), task.dueDate()));
        model.addAttribute("taskId", id);
        return "tasks/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("task") WebTaskForm task, BindingResult bindingResult,
                         Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, task);
            model.addAttribute("taskId", id);
            return "tasks/form";
        }
        taskService.updateVisibleTask(id, task.toTaskRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Task updated successfully");
        return "redirect:/tasks/" + id;
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        taskService.completeVisibleTask(id);
        redirectAttributes.addFlashAttribute("successMessage", "Task completed successfully");
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        taskService.deleteVisibleTask(id);
        redirectAttributes.addFlashAttribute("successMessage", "Task deleted successfully");
        return "redirect:/tasks";
    }

    private void addFormAttributes(Model model, WebTaskForm task) {
        model.addAttribute("task", task);
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
    }
}