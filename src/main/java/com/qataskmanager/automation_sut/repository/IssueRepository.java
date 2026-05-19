package com.qataskmanager.automation_sut.repository;

import com.qataskmanager.automation_sut.model.AppUser;
import com.qataskmanager.automation_sut.model.Issue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findAllByOrderById();
    List<Issue> findByCreatorOrAssignedUserOrderById(AppUser creator, AppUser assignedUser);
}
