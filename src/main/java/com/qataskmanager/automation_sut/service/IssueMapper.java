package com.qataskmanager.automation_sut.service;

import com.qataskmanager.automation_sut.dto.IssueDtos.AttachmentResponse;
import com.qataskmanager.automation_sut.dto.IssueDtos.CommentResponse;
import com.qataskmanager.automation_sut.dto.IssueDtos.IssueResponse;
import com.qataskmanager.automation_sut.model.Issue;
import com.qataskmanager.automation_sut.model.IssueAttachment;
import com.qataskmanager.automation_sut.model.IssueComment;
import org.springframework.stereotype.Component;

@Component
public class IssueMapper {
    private final LabelMapper labelMapper;

    public IssueMapper(LabelMapper labelMapper) {
        this.labelMapper = labelMapper;
    }

    public IssueResponse toResponse(Issue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStartDate(),
                issue.getEndDate(),
                issue.getCreator().getId(),
                issue.getCreator().getEmail(),
                issue.getAssignedUser().getId(),
                issue.getAssignedUser().getEmail(),
                issue.getStatus(),
                issue.getPriority(),
                issue.getLabels().stream().map(labelMapper::toResponse).toList(),
                issue.getComments().stream().map(this::toCommentResponse).toList()
        );
    }

    public CommentResponse toCommentResponse(IssueComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getId(),
                comment.getAuthor().getEmail(),
                comment.getCreatedAt(),
                comment.getAttachments().stream().map(this::toAttachmentResponse).toList()
        );
    }

    public AttachmentResponse toAttachmentResponse(IssueAttachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getUploadedAt()
        );
    }
}
