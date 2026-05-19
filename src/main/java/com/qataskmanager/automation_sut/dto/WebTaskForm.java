package com.qataskmanager.automation_sut.dto;

import com.qataskmanager.automation_sut.dto.TaskDtos.TaskRequest;
import com.qataskmanager.automation_sut.model.TaskPriority;
import com.qataskmanager.automation_sut.model.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class WebTaskForm {
    @NotBlank
    @Size(max = 100)
    private String title;

    @NotNull
    private TaskStatus status;

    @NotNull
    private TaskPriority priority;

    @NotNull
    @FutureOrPresent(message = "dueDate cannot be in the past")
    private LocalDate dueDate;

    public WebTaskForm() {
    }

    public WebTaskForm(String title, TaskStatus status, TaskPriority priority, LocalDate dueDate) {
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public TaskRequest toTaskRequest() {
        return new TaskRequest(title, status, priority, dueDate);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
