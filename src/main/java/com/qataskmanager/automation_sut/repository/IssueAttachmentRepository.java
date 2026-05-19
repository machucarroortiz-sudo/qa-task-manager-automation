package com.qataskmanager.automation_sut.repository;

import com.qataskmanager.automation_sut.model.IssueAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueAttachmentRepository extends JpaRepository<IssueAttachment, Long> {
}
