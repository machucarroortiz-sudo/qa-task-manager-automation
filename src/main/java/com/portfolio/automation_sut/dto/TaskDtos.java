package com.portfolio.automation_sut.dto;

import com.portfolio.automation_sut.model.TaskPriority;
import com.portfolio.automation_sut.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class TaskDtos {
    public record TaskRequest(
            @Schema(example = "Prepare API regression pack")
            @NotBlank
            @Size(max = 100)
            String title,

            @Schema(example = "TODO")
            @NotNull
            TaskStatus status,

            @Schema(example = "HIGH")
            @NotNull
            TaskPriority priority,

            @Schema(example = "2030-12-31")
            @NotNull
            @FutureOrPresent(message = "dueDate cannot be in the past")
            LocalDate dueDate
    ) {
    }

    public record TaskResponse(
            Long id,
            String title,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            Long ownerId,
            String ownerEmail
    ) {
    }
}
