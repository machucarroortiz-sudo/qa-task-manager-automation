package com.portfolio.automation_sut.service;

import com.portfolio.automation_sut.model.AppUser;
import com.portfolio.automation_sut.model.Issue;
import com.portfolio.automation_sut.model.IssueComment;
import com.portfolio.automation_sut.model.IssuePriority;
import com.portfolio.automation_sut.model.IssueStatus;
import com.portfolio.automation_sut.model.Label;
import com.portfolio.automation_sut.model.Role;
import com.portfolio.automation_sut.model.Task;
import com.portfolio.automation_sut.model.TaskPriority;
import com.portfolio.automation_sut.model.TaskStatus;
import com.portfolio.automation_sut.repository.IssueRepository;
import com.portfolio.automation_sut.repository.LabelRepository;
import com.portfolio.automation_sut.repository.TaskRepository;
import com.portfolio.automation_sut.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestDataService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final IssueRepository issueRepository;
    private final LabelRepository labelRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public TestDataService(UserRepository userRepository, TaskRepository taskRepository, IssueRepository issueRepository,
                           LabelRepository labelRepository, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.issueRepository = issueRepository;
        this.labelRepository = labelRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void reset() {
        issueRepository.deleteAll();
        issueRepository.flush();
        labelRepository.deleteAll();
        labelRepository.flush();
        taskRepository.deleteAll();
        taskRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();
        resetIdentityColumns();

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
    }

    private void resetIdentityColumns() {
        for (String table : new String[]{"app_users", "tasks", "labels", "issues", "issue_comments", "issue_attachments"}) {
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
}
