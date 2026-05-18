package com.portfolio.automation_sut.controller;

import com.portfolio.automation_sut.dto.IssueDtos.CommentRequest;
import com.portfolio.automation_sut.dto.WebIssueForm;
import com.portfolio.automation_sut.model.IssuePriority;
import com.portfolio.automation_sut.model.IssueStatus;
import com.portfolio.automation_sut.repository.UserRepository;
import com.portfolio.automation_sut.service.IssueService;
import com.portfolio.automation_sut.service.LabelService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.LinkedHashSet;
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

    public WebIssueController(IssueService issueService, LabelService labelService, UserRepository userRepository) {
        this.issueService = issueService;
        this.labelService = labelService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("issues", issueService.listVisibleIssues());
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
        redirectAttributes.addFlashAttribute("successMessage", "Issue created successfully");
        return "redirect:/issues/" + response.id();
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("issue", issueService.getVisibleIssue(id));
        model.addAttribute("comment", new CommentRequest(""));
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
        redirectAttributes.addFlashAttribute("successMessage", "Issue updated successfully");
        return "redirect:/issues/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        issueService.deleteIssue(id);
        redirectAttributes.addFlashAttribute("successMessage", "Issue deleted successfully");
        return "redirect:/issues";
    }

    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id, @Valid @ModelAttribute("comment") CommentRequest comment,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Comment text is required");
            return "redirect:/issues/" + id;
        }
        issueService.addComment(id, comment);
        redirectAttributes.addFlashAttribute("successMessage", "Comment added successfully");
        return "redirect:/issues/" + id;
    }

    @PostMapping("/{issueId}/comments/{commentId}/attachments")
    public String addAttachment(@PathVariable Long issueId, @PathVariable Long commentId, @RequestParam("file") MultipartFile file,
                                RedirectAttributes redirectAttributes) {
        try {
            issueService.addAttachment(issueId, commentId, file);
            redirectAttributes.addFlashAttribute("successMessage", "Attachment uploaded successfully");
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
    }
}
