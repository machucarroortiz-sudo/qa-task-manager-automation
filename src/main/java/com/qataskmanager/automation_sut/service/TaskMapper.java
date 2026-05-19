package com.qataskmanager.automation_sut.service;

import com.qataskmanager.automation_sut.dto.TaskDtos.TaskResponse;
import com.qataskmanager.automation_sut.model.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getOwner().getId(),
                task.getOwner().getEmail()
        );
    }
}
