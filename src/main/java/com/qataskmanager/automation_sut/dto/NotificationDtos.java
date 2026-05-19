package com.qataskmanager.automation_sut.dto;

import com.qataskmanager.automation_sut.model.NotificationType;
import java.time.Instant;

public class NotificationDtos {
    public record NotificationResponse(
            Long id,
            NotificationType type,
            String message,
            Instant createdAt,
            boolean read
    ) {
    }
}
