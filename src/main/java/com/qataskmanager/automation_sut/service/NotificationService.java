package com.qataskmanager.automation_sut.service;

import com.qataskmanager.automation_sut.dto.NotificationDtos.NotificationResponse;
import com.qataskmanager.automation_sut.model.AppUser;
import com.qataskmanager.automation_sut.model.Notification;
import com.qataskmanager.automation_sut.model.NotificationType;
import com.qataskmanager.automation_sut.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    public NotificationService(NotificationRepository notificationRepository, CurrentUserService currentUserService) {
        this.notificationRepository = notificationRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public long countUnreadForCurrentUser() {
        return notificationRepository.countByRecipientAndReadFalse(currentUserService.getCurrentUser());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listUnreadForCurrentUser() {
        return notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(currentUserService.getCurrentUser())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void notify(AppUser recipient, NotificationType type, String message) {
        notificationRepository.save(new Notification(recipient, type, message, Instant.now()));
    }

    @Transactional
    public void notifyIfDifferentUser(AppUser recipient, AppUser actor, NotificationType type, String message) {
        if (!recipient.getId().equals(actor.getId())) {
            notify(recipient, type, message);
        }
    }

    @Transactional
    public void markRead(Long id) {
        AppUser currentUser = currentUserService.getCurrentUser();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You cannot read another user's notification");
        }
        notification.markRead();
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.isRead()
        );
    }
}
