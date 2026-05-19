package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.dto.WebTaskForm;
import com.qataskmanager.automation_sut.model.Role;
import com.qataskmanager.automation_sut.model.TaskPriority;
import com.qataskmanager.automation_sut.model.TaskStatus;
import com.qataskmanager.automation_sut.service.CurrentUserService;
import com.qataskmanager.automation_sut.service.ShellModelService;
import com.qataskmanager.automation_sut.service.TaskService;
import jakarta.validation.Valid;
import java.util.stream.IntStream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tasks")
public class WebTaskController {
    private final TaskService taskService;
    private final CurrentUserService currentUserService;
    private final ShellModelService shellModelService;

    public WebTaskController(TaskService taskService, CurrentUserService currentUserService,
                             ShellModelService shellModelService) {
        this.taskService = taskService;
        this.currentUserService = currentUserService;
        this.shellModelService = shellModelService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) TaskStatus status,
                       @RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        var currentUser = currentUserService.getCurrentUser();
        var taskPage = taskService.searchVisibleTasks(status, null, search, page, size);
        model.addAttribute("tasks", taskPage.content());
        model.addAttribute("taskPage", taskPage);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search == null ? "" : search);
        addPaginationAttributes(model, taskPage.page(), taskPage.size(), taskPage.totalElements(), taskPage.totalPages());
        model.addAttribute("isAdmin", currentUser.getRole() == Role.ADMIN);
        shellModelService.addShellAttributes(model);
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
        shellModelService.addShellAttributes(model);
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
        shellModelService.addShellAttributes(model);
    }

    private void addPaginationAttributes(Model model, int page, int size, long totalElements, int totalPages) {
        long firstItem = totalElements == 0 ? 0 : (long) page * size + 1;
        long lastItem = Math.min((long) (page + 1) * size, totalElements);
        model.addAttribute("firstItem", firstItem);
        model.addAttribute("lastItem", lastItem);
        model.addAttribute("pageNumbers", totalPages == 0 ? java.util.List.of() : IntStream.range(0, totalPages).boxed().toList());
    }
}
