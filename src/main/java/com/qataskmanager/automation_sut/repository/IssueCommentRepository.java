package com.qataskmanager.automation_sut.repository;

import com.qataskmanager.automation_sut.model.Issue;
import com.qataskmanager.automation_sut.model.IssueComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {
    List<IssueComment> findByIssueOrderByCreatedAt(Issue issue);
}
