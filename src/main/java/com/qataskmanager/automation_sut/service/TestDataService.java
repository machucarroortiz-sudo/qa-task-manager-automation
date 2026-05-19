package com.qataskmanager.automation_sut.service;

import com.qataskmanager.automation_sut.dto.TestDataDtos.TestDataSummaryResponse;
import com.qataskmanager.automation_sut.exception.TestDataImportException;
import com.qataskmanager.automation_sut.model.AppUser;
import com.qataskmanager.automation_sut.model.Issue;
import com.qataskmanager.automation_sut.model.IssueComment;
import com.qataskmanager.automation_sut.model.IssuePriority;
import com.qataskmanager.automation_sut.model.IssueStatus;
import com.qataskmanager.automation_sut.model.Label;
import com.qataskmanager.automation_sut.model.Role;
import com.qataskmanager.automation_sut.model.Task;
import com.qataskmanager.automation_sut.model.TaskPriority;
import com.qataskmanager.automation_sut.model.TaskStatus;
import com.qataskmanager.automation_sut.repository.IssueRepository;
import com.qataskmanager.automation_sut.repository.LabelRepository;
import com.qataskmanager.automation_sut.repository.NotificationRepository;
import com.qataskmanager.automation_sut.repository.TaskRepository;
import com.qataskmanager.automation_sut.repository.UserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

@Service
public class TestDataService {
    private static final long MAX_IMPORT_FILE_SIZE = 1_000_000;

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final IssueRepository issueRepository;
    private final LabelRepository labelRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public TestDataService(UserRepository userRepository, TaskRepository taskRepository, IssueRepository issueRepository,
                           LabelRepository labelRepository, NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.issueRepository = issueRepository;
        this.labelRepository = labelRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TestDataSummaryResponse reset() {
        clearData();

        AppUser admin = userRepository.save(new AppUser("admin@example.com", passwordEncoder.encode("password123"), Role.ADMIN));
        AppUser user1 = userRepository.save(new AppUser("user1@example.com", passwordEncoder.encode("password123"), Role.USER));
        AppUser user2 = userRepository.save(new AppUser("user2@example.com", passwordEncoder.encode("password123"), Role.USER));

        LocalDate baseDate = LocalDate.now().plusDays(7);
        taskRepository.save(new Task("Review automation strategy", TaskStatus.TODO, TaskPriority.HIGH, baseDate, admin));
        taskRepository.save(new Task("Create UI smoke tests", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, baseDate.plusDays(1), user1));
        taskRepository.save(new Task("Prepare negative API cases", TaskStatus.TODO, TaskPriority.HIGH, baseDate.plusDays(2), user1));
        taskRepository.save(new Task("Update regression checklist", TaskStatus.DONE, TaskPriority.LOW, baseDate.plusDays(3), user2));
        taskRepository.save(new Task("Validate permission matrix", TaskStatus.TODO, TaskPriority.MEDIUM, baseDate.plusDays(4), user2));

        Label regression = labelRepository.save(new Label("regression", "#2868c7"));
        Label api = labelRepository.save(new Label("api", "#126b39"));
        Label ui = labelRepository.save(new Label("ui", "#7a2e0e"));
        Label security = labelRepository.save(new Label("security", "#b42318"));

        Issue issue1 = issueRepository.save(new Issue(
                "User cannot edit another user's issue",
                "RBAC negative case for issue modification via UI and API.",
                baseDate,
                baseDate.plusDays(5),
                user1,
                user1,
                IssueStatus.OPEN,
                IssuePriority.HIGH,
                Set.of(security, regression)
        ));
        issue1.getComments().add(new IssueComment(issue1, user1, "Seed comment for permission regression testing.", Instant.now()));

        Issue issue2 = issueRepository.save(new Issue(
                "Upload validation for issue comments",
                "Verify that PNG, JPG and MP4 files are accepted while unsupported files are rejected.",
                baseDate.plusDays(1),
                baseDate.plusDays(6),
                user2,
                user1,
                IssueStatus.IN_PROGRESS,
                IssuePriority.CRITICAL,
                Set.of(api, ui)
        ));
        issue2.getComments().add(new IssueComment(issue2, user2, "Seed comment for attachment upload testing.", Instant.now()));

        issueRepository.save(new Issue(
                "Admin reviews issue workflow",
                "Admin-visible issue used for dashboard and label management smoke checks.",
                baseDate.plusDays(2),
                baseDate.plusDays(9),
                admin,
                user2,
                IssueStatus.BLOCKED,
                IssuePriority.MEDIUM,
                Set.of(regression)
        ));
        return summary("Test data reset completed");
    }

    @Transactional
    public TestDataSummaryResponse clearAllData() {
        clearData();
        userRepository.save(new AppUser("admin@example.com", passwordEncoder.encode("password123"), Role.ADMIN));
        return summary("All SUT data cleared");
    }

    @Transactional
    public TestDataSummaryResponse loadDemoData() {
        try (InputStream inputStream = new ClassPathResource("demodata.json").getInputStream()) {
            return importData(inputStream, "Demo data loaded");
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not load demodata.json", exception);
        }
    }

    @Transactional
    public TestDataSummaryResponse importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Import file is required");
        }
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!filename.endsWith(".json")) {
            throw new IllegalArgumentException("Only JSON import files are supported");
        }
        if (file.getSize() > MAX_IMPORT_FILE_SIZE) {
            throw new IllegalArgumentException("Import file must be 1 MB or smaller");
        }
        try (InputStream inputStream = file.getInputStream()) {
            return importData(inputStream, "Test data imported");
        } catch (IOException exception) {
            throw new IllegalArgumentException("Import file could not be read", exception);
        }
    }

    private TestDataSummaryResponse importData(InputStream inputStream, String message) throws IOException {
        TestDataImport data = objectMapper.readValue(inputStream, TestDataImport.class);
        validateImport(data);
        clearData();

        Map<String, AppUser> users = new LinkedHashMap<>();
        for (ImportUser user : data.users()) {
            users.put(user.email(), userRepository.save(new AppUser(user.email(), passwordEncoder.encode(user.password()), user.role())));
        }

        Map<String, Label> labels = new LinkedHashMap<>();
        for (ImportLabel label : data.labels()) {
            labels.put(label.name(), labelRepository.save(new Label(label.name(), label.color())));
        }

        for (ImportTask task : data.tasks()) {
            taskRepository.save(new Task(task.title(), task.status(), task.priority(), task.dueDate(), users.get(task.ownerEmail())));
        }

        for (ImportIssue issue : data.issues()) {
            Set<Label> issueLabels = issue.labelNames().stream()
                    .map(labels::get)
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
            Issue savedIssue = issueRepository.save(new Issue(
                    issue.title(),
                    issue.description(),
                    issue.startDate(),
                    issue.endDate(),
                    users.get(issue.creatorEmail()),
                    users.get(issue.assignedUserEmail()),
                    issue.status(),
                    issue.priority(),
                    issueLabels
            ));
            for (ImportComment comment : issue.comments()) {
                savedIssue.getComments().add(new IssueComment(savedIssue, users.get(comment.authorEmail()), comment.text(), comment.createdAt()));
            }
        }
        return summary(message);
    }

    private void clearData() {
        notificationRepository.deleteAll();
        notificationRepository.flush();
        issueRepository.deleteAll();
        issueRepository.flush();
        labelRepository.deleteAll();
        labelRepository.flush();
        taskRepository.deleteAll();
        taskRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();
        resetIdentityColumns();
    }

    private void validateImport(TestDataImport data) {
        if (data == null || data.users() == null || data.labels() == null || data.tasks() == null || data.issues() == null) {
            fail("root", "Import file must include users, labels, tasks, and issues");
        }
        Map<String, ImportUser> users = new LinkedHashMap<>();
        for (ImportUser user : data.users()) {
            requireText(user.email(), "User email is required");
            requireText(user.password(), "User password is required");
            if (user.role() == null) {
                fail("users.role", "User role is required");
            }
            users.put(user.email(), user);
        }
        for (String email : List.of("admin@example.com", "user1@example.com", "user2@example.com")) {
            if (!users.containsKey(email)) {
                fail("users", "Import file must include " + email);
            }
        }

        Map<String, ImportLabel> labels = new LinkedHashMap<>();
        for (ImportLabel label : data.labels()) {
            requireText(label.name(), "Label name is required");
            requireText(label.color(), "Label color is required");
            labels.put(label.name(), label);
        }

        for (ImportTask task : data.tasks()) {
            requireText(task.title(), "Task title is required");
            requireUser(users, task.ownerEmail());
            if (task.status() == null || task.priority() == null || task.dueDate() == null) {
                fail("tasks", "Task status, priority, and dueDate are required");
            }
        }

        for (ImportIssue issue : data.issues()) {
            requireText(issue.title(), "Issue title is required");
            requireText(issue.description(), "Issue description is required");
            requireUser(users, issue.creatorEmail());
            requireUser(users, issue.assignedUserEmail());
            if (issue.status() == null || issue.priority() == null || issue.startDate() == null || issue.endDate() == null) {
                fail("issues", "Issue status, priority, startDate, and endDate are required");
            }
            if (issue.endDate().isBefore(issue.startDate())) {
                fail("issues.endDate", "Issue endDate cannot be before startDate");
            }
            for (String labelName : issue.labelNames()) {
                if (!labels.containsKey(labelName)) {
                    fail("issues.labelNames", "Unknown label in issue import: " + labelName);
                }
            }
            for (ImportComment comment : issue.comments()) {
                requireUser(users, comment.authorEmail());
                requireText(comment.text(), "Comment text is required");
            }
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            fail("import", message);
        }
    }

    private void requireUser(Map<String, ImportUser> users, String email) {
        requireText(email, "User email reference is required");
        if (!users.containsKey(email)) {
            fail("userReference", "Unknown user in import: " + email);
        }
    }

    private void fail(String field, String message) {
        throw new TestDataImportException(message, Map.of(field, message));
    }

    private void resetIdentityColumns() {
        for (String table : new String[]{"app_users", "tasks", "labels", "issues", "issue_comments", "issue_attachments", "notifications"}) {
            tryExecute("alter table " + table + " alter column id restart with 1");
            tryExecute("alter sequence " + table + "_id_seq restart with 1");
        }
    }

    private void tryExecute(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (RuntimeException ignored) {
            // H2 and PostgreSQL use different identity reset commands; unsupported commands are ignored deliberately.
        }
    }

    private TestDataSummaryResponse summary(String message) {
        long comments = issueRepository.findAll().stream().mapToLong(issue -> issue.getComments().size()).sum();
        return new TestDataSummaryResponse(
                message,
                userRepository.count(),
                taskRepository.count(),
                issueRepository.count(),
                labelRepository.count(),
                comments
        );
    }

    public record TestDataImport(
            List<ImportUser> users,
            List<ImportLabel> labels,
            List<ImportTask> tasks,
            List<ImportIssue> issues
    ) {
    }

    public record ImportUser(String email, String password, Role role) {
    }

    public record ImportLabel(String name, String color) {
    }

    public record ImportTask(String title, TaskStatus status, TaskPriority priority, LocalDate dueDate, String ownerEmail) {
    }

    public record ImportIssue(String title, String description, LocalDate startDate, LocalDate endDate,
                              String creatorEmail, String assignedUserEmail, IssueStatus status, IssuePriority priority,
                              List<String> labelNames, List<ImportComment> comments) {
        public ImportIssue {
            labelNames = labelNames == null ? List.of() : List.copyOf(labelNames);
            comments = comments == null ? new ArrayList<>() : List.copyOf(comments);
        }
    }

    public record ImportComment(String authorEmail, String text, Instant createdAt) {
        public ImportComment {
            createdAt = createdAt == null ? Instant.now() : createdAt;
        }
    }
}
