package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.service.ShellModelService;
import com.qataskmanager.automation_sut.service.TaskService;
import java.util.stream.IntStream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class WebAdminController {
    private final TaskService taskService;
    private final ShellModelService shellModelService;

    public WebAdminController(TaskService taskService, ShellModelService shellModelService) {
        this.taskService = taskService;
        this.shellModelService = shellModelService;
    }

    @GetMapping
    public String dashboard(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        var taskPage = taskService.searchVisibleTasks(null, null, null, page, size);
        model.addAttribute("tasks", taskPage.content());
        model.addAttribute("taskPage", taskPage);
        addPaginationAttributes(model, taskPage.page(), taskPage.size(), taskPage.totalElements(), taskPage.totalPages());
        shellModelService.addShellAttributes(model);
        return "admin/dashboard";
    }

    private void addPaginationAttributes(Model model, int page, int size, long totalElements, int totalPages) {
        long firstItem = totalElements == 0 ? 0 : (long) page * size + 1;
        long lastItem = Math.min((long) (page + 1) * size, totalElements);
        model.addAttribute("firstItem", firstItem);
        model.addAttribute("lastItem", lastItem);
        model.addAttribute("pageNumbers", totalPages == 0 ? java.util.List.of() : IntStream.range(0, totalPages).boxed().toList());
    }
}
