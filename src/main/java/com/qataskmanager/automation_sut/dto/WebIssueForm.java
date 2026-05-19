package com.qataskmanager.automation_sut.dto;

import com.qataskmanager.automation_sut.dto.IssueDtos.IssueRequest;
import com.qataskmanager.automation_sut.model.IssuePriority;
import com.qataskmanager.automation_sut.model.IssueStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

public class WebIssueForm {
    @NotBlank(message = "{validation.issue.title.required}")
    @Size(max = 120, message = "{validation.issue.title.size}")
    private String title;

    @NotBlank(message = "{validation.issue.description.required}")
    @Size(max = 2000, message = "{validation.issue.description.size}")
    private String description;

    @NotNull(message = "{validation.issue.startDate.required}")
    private LocalDate startDate;

    @NotNull(message = "{validation.issue.endDate.required}")
    private LocalDate endDate;

    @NotNull(message = "{validation.issue.assignedUser.required}")
    private Long assignedUserId;

    @NotNull(message = "{validation.issue.status.required}")
    private IssueStatus status;

    @NotNull(message = "{validation.issue.priority.required}")
    private IssuePriority priority;

    private Set<Long> labelIds = new LinkedHashSet<>();

    public WebIssueForm() {
    }

    public WebIssueForm(String title, String description, LocalDate startDate, LocalDate endDate, Long assignedUserId,
                        IssueStatus status, IssuePriority priority, Set<Long> labelIds) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.assignedUserId = assignedUserId;
        this.status = status;
        this.priority = priority;
        this.labelIds = labelIds;
    }

    @AssertTrue(message = "{validation.issue.dateRange}")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }

    public IssueRequest toIssueRequest() {
        return new IssueRequest(title, description, startDate, endDate, assignedUserId, status, priority, labelIds);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Long assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public IssuePriority getPriority() {
        return priority;
    }

    public void setPriority(IssuePriority priority) {
        this.priority = priority;
    }

    public Set<Long> getLabelIds() {
        return labelIds;
    }

    public void setLabelIds(Set<Long> labelIds) {
        this.labelIds = labelIds;
    }
}
