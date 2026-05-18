package com.portfolio.automation_sut.service;

import com.portfolio.automation_sut.dto.IssueDtos.AttachmentResponse;
import com.portfolio.automation_sut.dto.IssueDtos.CommentRequest;
import com.portfolio.automation_sut.dto.IssueDtos.CommentResponse;
import com.portfolio.automation_sut.dto.IssueDtos.IssueRequest;
import com.portfolio.automation_sut.dto.IssueDtos.IssueResponse;
import com.portfolio.automation_sut.model.AppUser;
import com.portfolio.automation_sut.model.Issue;
import com.portfolio.automation_sut.model.IssueAttachment;
import com.portfolio.automation_sut.model.IssueComment;
import com.portfolio.automation_sut.model.Label;
import com.portfolio.automation_sut.model.Role;
import com.portfolio.automation_sut.repository.IssueAttachmentRepository;
import com.portfolio.automation_sut.repository.IssueCommentRepository;
import com.portfolio.automation_sut.repository.IssueRepository;
import com.portfolio.automation_sut.repository.LabelRepository;
import com.portfolio.automation_sut.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IssueService {
    public static final long MAX_ATTACHMENT_SIZE_BYTES = 5 * 1024 * 1024;

    private final IssueRepository issueRepository;
    private final IssueCommentRepository commentRepository;
    private final IssueAttachmentRepository attachmentRepository;
    private final LabelRepository labelRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final IssueMapper issueMapper;

    public IssueService(IssueRepository issueRepository, IssueCommentRepository commentRepository,
                        IssueAttachmentRepository attachmentRepository, LabelRepository labelRepository,
                        UserRepository userRepository, CurrentUserService currentUserService, IssueMapper issueMapper) {
        this.issueRepository = issueRepository;
        this.commentRepository = commentRepository;
        this.attachmentRepository = attachmentRepository;
        this.labelRepository = labelRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.issueMapper = issueMapper;
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> listVisibleIssues() {
        AppUser currentUser = currentUserService.getCurrentUser();
        List<Issue> issues = isAdmin(currentUser)
                ? issueRepository.findAllByOrderById()
                : issueRepository.findByCreatorOrAssignedUserOrderById(currentUser, currentUser);
        return issues.stream().map(issueMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public IssueResponse getVisibleIssue(Long id) {
        return issueMapper.toResponse(findVisibleIssue(id));
    }

    @Transactional
    public IssueResponse createIssue(IssueRequest request) {
        AppUser creator = currentUserService.getCurrentUser();
        AppUser assignedUser = findUser(request.assignedUserId());
        Issue issue = new Issue(
                request.title(),
                request.description(),
                request.startDate(),
                request.endDate(),
                creator,
                assignedUser,
                request.status(),
                request.priority(),
                findLabels(request.labelIds())
        );
        return issueMapper.toResponse(issueRepository.save(issue));
    }

    @Transactional
    public IssueResponse updateIssue(Long id, IssueRequest request) {
        Issue issue = findEditableIssue(id);
        issue.update(
                request.title(),
                request.description(),
                request.startDate(),
                request.endDate(),
                findUser(request.assignedUserId()),
                request.status(),
                request.priority(),
                findLabels(request.labelIds())
        );
        return issueMapper.toResponse(issue);
    }

    @Transactional
    public void deleteIssue(Long id) {
        issueRepository.delete(findEditableIssue(id));
    }

    @Transactional
    public CommentResponse addComment(Long issueId, CommentRequest request) {
        Issue issue = findVisibleIssue(issueId);
        IssueComment comment = new IssueComment(issue, currentUserService.getCurrentUser(), request.text(), Instant.now());
        return issueMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long issueId) {
        Issue issue = findVisibleIssue(issueId);
        return commentRepository.findByIssueOrderByCreatedAt(issue).stream().map(issueMapper::toCommentResponse).toList();
    }

    @Transactional
    public AttachmentResponse addAttachment(Long issueId, Long commentId, MultipartFile file) {
        IssueComment comment = findVisibleComment(issueId, commentId);
        validateAttachment(file);
        try {
            IssueAttachment attachment = new IssueAttachment(
                    comment,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    Instant.now(),
                    file.getBytes()
            );
            return issueMapper.toAttachmentResponse(attachmentRepository.save(attachment));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Attachment could not be read");
        }
    }

    @Transactional(readOnly = true)
    public IssueAttachment getAttachment(Long issueId, Long commentId, Long attachmentId) {
        IssueComment comment = findVisibleComment(issueId, commentId);
        IssueAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));
        if (!attachment.getComment().getId().equals(comment.getId())) {
            throw new EntityNotFoundException("Attachment not found");
        }
        return attachment;
    }

    private Issue findVisibleIssue(Long id) {
        AppUser currentUser = currentUserService.getCurrentUser();
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Issue not found"));
        if (!isAdmin(currentUser)
                && !issue.getCreator().getId().equals(currentUser.getId())
                && !issue.getAssignedUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You cannot access issues that are not created by or assigned to you");
        }
        return issue;
    }

    private Issue findEditableIssue(Long id) {
        AppUser currentUser = currentUserService.getCurrentUser();
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Issue not found"));
        if (!isAdmin(currentUser) && !issue.getCreator().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You cannot modify issues created by another user");
        }
        return issue;
    }

    private IssueComment findVisibleComment(Long issueId, Long commentId) {
        Issue issue = findVisibleIssue(issueId);
        IssueComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        if (!comment.getIssue().getId().equals(issue.getId())) {
            throw new EntityNotFoundException("Comment not found");
        }
        return comment;
    }

    private AppUser findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assigned user not found"));
    }

    private Set<Label> findLabels(Set<Long> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        List<Label> labels = labelRepository.findAllById(labelIds);
        if (labels.size() != labelIds.size()) {
            throw new EntityNotFoundException("One or more labels were not found");
        }
        return new LinkedHashSet<>(labels);
    }

    private void validateAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Attachment file is required");
        }
        if (file.getSize() > MAX_ATTACHMENT_SIZE_BYTES) {
            throw new IllegalArgumentException("Attachment file size must be 5 MB or less");
        }
        String contentType = file.getContentType();
        if (!MediaType.IMAGE_PNG_VALUE.equals(contentType)
                && !MediaType.IMAGE_JPEG_VALUE.equals(contentType)
                && !"video/mp4".equals(contentType)) {
            throw new IllegalArgumentException("Attachment file type must be PNG, JPG, or MP4");
        }
    }

    private boolean isAdmin(AppUser user) {
        return user.getRole() == Role.ADMIN;
    }
}
