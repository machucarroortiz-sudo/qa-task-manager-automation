package com.qataskmanager.automation_sut.controller;

import com.qataskmanager.automation_sut.dto.IssueDtos.CommentRequest;
import com.qataskmanager.automation_sut.dto.WebIssueForm;
import com.qataskmanager.automation_sut.model.IssuePriority;
import com.qataskmanager.automation_sut.model.IssueStatus;
import com.qataskmanager.automation_sut.repository.UserRepository;
import com.qataskmanager.automation_sut.service.IssueService;
import com.qataskmanager.automation_sut.service.LabelService;
import com.qataskmanager.automation_sut.service.ShellModelService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.stream.IntStream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/issues")
public class WebIssueController {
    private final IssueService issueService;
    private final LabelService labelService;
    private final UserRepository userRepository;
    private final ShellModelService shellModelService;

    public WebIssueController(IssueService issueService, LabelService labelService, UserRepository userRepository,
                              ShellModelService shellModelService) {
        this.issueService = issueService;
        this.labelService = labelService;
        this.userRepository = userRepository;
        this.shellModelService = shellModelService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) IssueStatus status,
                       @RequestParam(required = false) IssuePriority priority,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String label,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        var issuePage = issueService.searchVisibleIssues(status, priority, search, label, page, size);
        model.addAttribute("issues", issuePage.content());
        model.addAttribute("issuePage", issuePage);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("label", label == null ? "" : label);
        addPaginationAttributes(model, issuePage.page(), issuePage.size(), issuePage.totalElements(), issuePage.totalPages());
        shellModelService.addShellAttributes(model);
        return "issues/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        addFormAttributes(model, new WebIssueForm("", "", LocalDate.now(), LocalDate.now().plusDays(7),
                null, IssueStatus.OPEN, IssuePriority.MEDIUM, new LinkedHashSet<>()));
        return "issues/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("issue") WebIssueForm issue, BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, issue);
            return "issues/form";
        }
        var response = issueService.createIssue(issue.toIssueRequest());
        redirectAttributes.addFlashAttribute("successMessageKey", "flash.issue.created");
        return "redirect:/issues/" + response.id();
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("issue", issueService.getVisibleIssue(id));
        model.addAttribute("comment", new CommentRequest(""));
        shellModelService.addShellAttributes(model);
        return "issues/details";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var issue = issueService.getVisibleIssue(id);
        addFormAttributes(model, new WebIssueForm(
                issue.title(),
                issue.description(),
                issue.startDate(),
                issue.endDate(),
                issue.assignedUserId(),
                issue.status(),
                issue.priority(),
                issue.labels().stream().map(label -> label.id()).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
        ));
        model.addAttribute("issueId", id);
        return "issues/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("issue") WebIssueForm issue,
                         BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, issue);
            model.addAttribute("issueId", id);
            return "issues/form";
        }
        issueService.updateIssue(id, issue.toIssueRequest());
        redirectAttributes.addFlashAttribute("successMessageKey", "flash.issue.updated");
        return "redirect:/issues/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        issueService.deleteIssue(id);
        redirectAttributes.addFlashAttribute("successMessageKey", "flash.issue.deleted");
        return "redirect:/issues";
    }

    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id, @Valid @ModelAttribute("comment") CommentRequest comment,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessageKey", "flash.comment.required");
            return "redirect:/issues/" + id;
        }
        issueService.addComment(id, comment);
        redirectAttributes.addFlashAttribute("successMessageKey", "flash.comment.added");
        return "redirect:/issues/" + id;
    }

    @PostMapping("/{issueId}/comments/{commentId}/attachments")
    public String addAttachment(@PathVariable Long issueId, @PathVariable Long commentId, @RequestParam("file") MultipartFile file,
                                RedirectAttributes redirectAttributes) {
        try {
            issueService.addAttachment(issueId, commentId, file);
            redirectAttributes.addFlashAttribute("successMessageKey", "flash.attachment.uploaded");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/issues/" + issueId;
    }

    private void addFormAttributes(Model model, WebIssueForm issue) {
        model.addAttribute("issue", issue);
        model.addAttribute("statuses", IssueStatus.values());
        model.addAttribute("priorities", IssuePriority.values());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("labels", labelService.listLabels());
        shellModelService.addShellAttributes(model);
    }

    private void addPaginationAttributes(Model model, int page, int size, long totalElements, int totalPages) {
        long firstItem = totalElements == 0 ? 0 : (long) page * size + 1;
        long lastItem = Math.min((long) (page + 1) * size, totalElements);
        model.addAttribute("firstItem", firstItem);
        model.addAttribute("lastItem", lastItem);
        model.addAttribute("pageNumbers", totalPages == 0 ? java.util.List.of() : IntStream.range(0, totalPages).boxed().toList());
    }
}
