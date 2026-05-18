package com.portfolio.automation_sut.repository;

import com.portfolio.automation_sut.model.IssueAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueAttachmentRepository extends JpaRepository<IssueAttachment, Long> {
}
