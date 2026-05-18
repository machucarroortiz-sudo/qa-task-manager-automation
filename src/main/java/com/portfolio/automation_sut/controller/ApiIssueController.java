package com.portfolio.automation_sut.controller;

import com.portfolio.automation_sut.dto.IssueDtos.AttachmentResponse;
import com.portfolio.automation_sut.dto.IssueDtos.CommentRequest;
import com.portfolio.automation_sut.dto.IssueDtos.CommentResponse;
import com.portfolio.automation_sut.dto.IssueDtos.IssueRequest;
import com.portfolio.automation_sut.dto.IssueDtos.IssueResponse;
import com.portfolio.automation_sut.model.IssueAttachment;
import com.portfolio.automation_sut.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/issues")
public class ApiIssueController {
    private final IssueService issueService;

    public ApiIssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @Operation(summary = "List issues visible to the current user")
    @GetMapping
    public List<IssueResponse> listIssues() {
        return issueService.listVisibleIssues();
    }

    @Operation(summary = "Create an issue owned by the current user")
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(@Valid @RequestBody IssueRequest request) {
        IssueResponse response = issueService.createIssue(request);
        return ResponseEntity.created(URI.create("/api/issues/" + response.id())).body(response);
    }

    @Operation(summary = "Get an issue if the current user is allowed to see it")
    @GetMapping("/{id}")
    public IssueResponse getIssue(@PathVariable Long id) {
        return issueService.getVisibleIssue(id);
    }

    @Operation(summary = "Update an issue if the current user is creator or admin")
    @PutMapping("/{id}")
    public IssueResponse updateIssue(@PathVariable Long id, @Valid @RequestBody IssueRequest request) {
        return issueService.updateIssue(id, request);
    }

    @Operation(summary = "Delete an issue if the current user is creator or admin")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssue(@PathVariable Long id) {
        issueService.deleteIssue(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List comments for a visible issue")
    @GetMapping("/{id}/comments")
    public List<CommentResponse> listComments(@PathVariable Long id) {
        return issueService.listComments(id);
    }

    @Operation(summary = "Add a comment to a visible issue")
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        CommentResponse response = issueService.addComment(id, request);
        return ResponseEntity.created(URI.create("/api/issues/" + id + "/comments/" + response.id())).body(response);
    }

    @Operation(summary = "Upload a PNG, JPG, or MP4 attachment to a comment")
    @PostMapping(path = "/{issueId}/comments/{commentId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> addAttachment(@PathVariable Long issueId, @PathVariable Long commentId,
                                                            @RequestPart("file") MultipartFile file) {
        AttachmentResponse response = issueService.addAttachment(issueId, commentId, file);
        return ResponseEntity.created(URI.create("/api/issues/" + issueId + "/comments/" + commentId + "/attachments/" + response.id()))
                .body(response);
    }

    @Operation(summary = "Download an attachment from a visible issue comment")
    @GetMapping("/{issueId}/comments/{commentId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> getAttachment(@PathVariable Long issueId, @PathVariable Long commentId,
                                                @PathVariable Long attachmentId) {
        IssueAttachment attachment = issueService.getAttachment(issueId, commentId, attachmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(attachment.getFileName())
                        .build()
                        .toString())
                .body(attachment.getData());
    }
}
