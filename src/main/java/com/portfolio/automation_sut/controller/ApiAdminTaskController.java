package com.portfolio.automation_sut.controller;

import com.portfolio.automation_sut.dto.TaskDtos.TaskRequest;
import com.portfolio.automation_sut.dto.TaskDtos.TaskResponse;
import com.portfolio.automation_sut.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/tasks")
public class ApiAdminTaskController {
    private final TaskService taskService;

    public ApiAdminTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "List all tasks. Admin only.")
    @GetMapping
    public List<TaskResponse> allTasks() {
        return taskService.listVisibleTasks();
    }

    @Operation(summary = "Update any task. Admin only.")
    @PutMapping("/{id}")
    public TaskResponse updateAnyTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.updateVisibleTask(id, request);
    }

    @Operation(summary = "Delete any task. Admin only.")
    @DeleteMapping("/{id}")
    public void deleteAnyTask(@PathVariable Long id) {
        taskService.deleteVisibleTask(id);
    }
}
