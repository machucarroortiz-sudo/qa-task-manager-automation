package com.qataskmanager.automation_sut.service;

import com.qataskmanager.automation_sut.dto.IssueDtos.AttachmentResponse;
import com.qataskmanager.automation_sut.dto.IssueDtos.CommentRequest;
import com.qataskmanager.automation_sut.dto.IssueDtos.CommentResponse;
import com.qataskmanager.automation_sut.dto.IssueDtos.IssueRequest;
import com.qataskmanager.automation_sut.dto.IssueDtos.IssueResponse;
import com.qataskmanager.automation_sut.dto.PaginationDtos.PagedResponse;
import com.qataskmanager.automation_sut.model.AppUser;
import com.qataskmanager.automation_sut.model.Issue;
import com.qataskmanager.automation_sut.model.IssueAttachment;
import com.qataskmanager.automation_sut.model.IssueComment;
import com.qataskmanager.automation_sut.model.IssuePriority;
import com.qataskmanager.automation_sut.model.IssueStatus;
import com.qataskmanager.automation_sut.model.Label;
import com.qataskmanager.automation_sut.model.NotificationType;
import com.qataskmanager.automation_sut.model.Role;
import com.qataskmanager.automation_sut.repository.IssueAttachmentRepository;
import com.qataskmanager.automation_sut.repository.IssueCommentRepository;
import com.qataskmanager.automation_sut.repository.IssueRepository;
import com.qataskmanager.automation_sut.repository.LabelRepository;
import com.qataskmanager.automation_sut.repository.UserRepository;
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
    private final NotificationService notificationService;

    public IssueService(IssueRepository issueRepository, IssueCommentRepository commentRepository,
                        IssueAttachmentRepository attachmentRepository, LabelRepository labelRepository,
                        UserRepository userRepository, CurrentUserService currentUserService, IssueMapper issueMapper,
                        NotificationService notificationService) {
        this.issueRepository = issueRepository;
        this.commentRepository = commentRepository;
        this.attachmentRepository = attachmentRepository;
        this.labelRepository = labelRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.issueMapper = issueMapper;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> listVisibleIssues() {
        return listVisibleIssueEntities().stream().map(issueMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> searchVisibleIssues(IssueStatus status, IssuePriority priority, String search, String label) {
        String normalizedSearch = normalize(search);
        String normalizedLabel = normalize(label);
        return filterVisibleIssueEntities(status, priority, normalizedSearch, normalizedLabel).stream()
                .map(issueMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<IssueResponse> searchVisibleIssues(IssueStatus status, IssuePriority priority, String search, String label,
                                                           int page, int size) {
        String normalizedSearch = normalize(search);
        String normalizedLabel = normalize(label);
        List<IssueResponse> filteredIssues = filterVisibleIssueEntities(status, priority, normalizedSearch, normalizedLabel).stream()
                .map(issueMapper::toResponse)
                .toList();
        return page(filteredIssues, page, size);
    }

    private List<Issue> filterVisibleIssueEntities(IssueStatus status, IssuePriority priority, String normalizedSearch, String normalizedLabel) {
        return listVisibleIssueEntities().stream()
                .filter(issue -> status == null || issue.getStatus() == status)
                .filter(issue -> priority == null || issue.getPriority() == priority)
                .filter(issue -> normalizedSearch.isBlank() || issueMatchesSearch(issue, normalizedSearch))
                .filter(issue -> normalizedLabel.isBlank() || issue.getLabels().stream()
                        .anyMatch(issueLabel -> normalize(issueLabel.getName()).contains(normalizedLabel)))
                .toList();
    }

    private List<Issue> listVisibleIssueEntities() {
        AppUser currentUser = currentUserService.getCurrentUser();
        return isAdmin(currentUser)
                ? issueRepository.findAllByOrderById()
                : issueRepository.findByCreatorOrAssignedUserOrderById(currentUser, currentUser);
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
        AppUser previousAssignedUser = issue.getAssignedUser();
        var previousStatus = issue.getStatus();
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
        AppUser actor = currentUserService.getCurrentUser();
        String message = previousStatus == request.status()
                ? "Issue updated: " + issue.getTitle()
                : "Issue status changed to " + request.status() + ": " + issue.getTitle();
        notificationService.notifyIfDifferentUser(issue.getCreator(), actor, NotificationType.ISSUE, message);
        if (!issue.getAssignedUser().getId().equals(issue.getCreator().getId())) {
            notificationService.notifyIfDifferentUser(issue.getAssignedUser(), actor, NotificationType.ISSUE, message);
        }
        if (!previousAssignedUser.getId().equals(issue.getAssignedUser().getId())) {
            notificationService.notifyIfDifferentUser(
                    previousAssignedUser,
                    actor,
                    NotificationType.ISSUE,
                    "Issue reassigned: " + issue.getTitle()
            );
        }
        return issueMapper.toResponse(issue);
    }

    @Transactional
    public void deleteIssue(Long id) {
        issueRepository.delete(findEditableIssue(id));
    }

    @Transactional
    public CommentResponse addComment(Long issueId, CommentRequest request) {
        Issue issue = findVisibleIssue(issueId);
        AppUser actor = currentUserService.getCurrentUser();
        IssueComment comment = new IssueComment(issue, actor, request.text(), Instant.now());
        String message = "Comment added to issue: " + issue.getTitle();
        notificationService.notifyIfDifferentUser(issue.getCreator(), actor, NotificationType.ISSUE, message);
        if (!issue.getAssignedUser().getId().equals(issue.getCreator().getId())) {
            notificationService.notifyIfDifferentUser(issue.getAssignedUser(), actor, NotificationType.ISSUE, message);
        }
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

    private boolean issueMatchesSearch(Issue issue, String normalizedSearch) {
        return normalize(issue.getTitle()).contains(normalizedSearch)
                || issue.getLabels().stream().anyMatch(label -> normalize(label.getName()).contains(normalizedSearch));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private PagedResponse<IssueResponse> page(List<IssueResponse> items, int requestedPage, int requestedSize) {
        int safeSize = Math.max(1, Math.min(requestedSize, 100));
        int totalPages = items.isEmpty() ? 0 : (int) Math.ceil(items.size() / (double) safeSize);
        int safePage = totalPages == 0 ? 0 : Math.max(0, Math.min(requestedPage, totalPages - 1));
        int fromIndex = Math.min(safePage * safeSize, items.size());
        int toIndex = Math.min(fromIndex + safeSize, items.size());
        return new PagedResponse<>(
                items.subList(fromIndex, toIndex),
                safePage,
                safeSize,
                items.size(),
                totalPages,
                safePage == 0,
                totalPages == 0 || safePage >= totalPages - 1
        );
    }
}
