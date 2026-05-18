package com.portfolio.automation_sut.controller;

import com.portfolio.automation_sut.dto.TaskDtos.TaskRequest;
import com.portfolio.automation_sut.dto.TaskDtos.TaskResponse;
import com.portfolio.automation_sut.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class ApiTaskController {
    private final TaskService taskService;

    public ApiTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "List tasks visible to the current user")
    @GetMapping
    public List<TaskResponse> listTasks() {
        return taskService.listVisibleTasks();
    }

    @Operation(summary = "Create a task owned by the current user")
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.created(URI.create("/api/tasks/" + response.id())).body(response);
    }

    @Operation(summary = "Get a task if the current user is allowed to see it")
    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id) {
        return taskService.getVisibleTask(id);
    }

    @Operation(summary = "Update a task if the current user is allowed to manage it")
    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.updateVisibleTask(id, request);
    }

    @Operation(summary = "Mark a task as DONE")
    @PostMapping("/{id}/complete")
    public TaskResponse completeTask(@PathVariable Long id) {
        return taskService.completeVisibleTask(id);
    }

    @Operation(summary = "Delete a task if the current user is allowed to manage it")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteVisibleTask(id);
        return ResponseEntity.noContent().build();
    }
}
