package com.qataskmanager.automation_sut.repository;

import com.qataskmanager.automation_sut.model.AppUser;
import com.qataskmanager.automation_sut.model.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByRecipientAndReadFalse(AppUser recipient);
    List<Notification> findByRecipientAndReadFalseOrderByCreatedAtDesc(AppUser recipient);
}
