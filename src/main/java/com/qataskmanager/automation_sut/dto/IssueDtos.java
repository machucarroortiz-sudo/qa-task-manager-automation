package com.qataskmanager.automation_sut.dto;

import com.qataskmanager.automation_sut.dto.LabelDtos.LabelResponse;
import com.qataskmanager.automation_sut.model.IssuePriority;
import com.qataskmanager.automation_sut.model.IssueStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class IssueDtos {
    public record IssueRequest(
            @Schema(example = "API returns 403 for another user's issue")
            @NotBlank
            @Size(max = 120)
            String title,

            @Schema(example = "Verify that users cannot modify issues created by another user.")
            @NotBlank
            @Size(max = 2000)
            String description,

            @Schema(example = "2026-05-18")
            @NotNull
            LocalDate startDate,

            @Schema(example = "2026-05-25")
            @NotNull
            LocalDate endDate,

            @Schema(example = "2")
            @NotNull
            Long assignedUserId,

            @Schema(example = "OPEN")
            @NotNull
            IssueStatus status,

            @Schema(example = "HIGH")
            @NotNull
            IssuePriority priority,

            @Schema(example = "[1, 2]")
            Set<Long> labelIds
    ) {
        @AssertTrue(message = "endDate cannot be before startDate")
        public boolean isDateRangeValid() {
            return startDate == null || endDate == null || !endDate.isBefore(startDate);
        }
    }

    public record IssueResponse(
            Long id,
            String title,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            Long creatorId,
            String creatorEmail,
            Long assignedUserId,
            String assignedUserEmail,
            IssueStatus status,
            IssuePriority priority,
            List<LabelResponse> labels,
            List<CommentResponse> comments
    ) {
    }

    public record CommentRequest(
            @Schema(example = "Reproduced in Chrome and Edge.")
            @NotBlank
            @Size(max = 1000)
            String text
    ) {
    }

    public record CommentResponse(
            Long id,
            String text,
            Long authorId,
            String authorEmail,
            Instant createdAt,
            List<AttachmentResponse> attachments
    ) {
    }

    public record AttachmentResponse(
            Long id,
            String fileName,
            String contentType,
            long fileSize,
            Instant uploadedAt
    ) {
    }
}
