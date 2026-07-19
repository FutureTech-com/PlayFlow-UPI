package com.payflow.service;

import com.payflow.dto.notification.NotificationResponse;
import com.payflow.entity.Notification;
import com.payflow.entity.User;
import com.payflow.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

	public Notification createNotification(User user, String title, String message, Notification.NotificationType type) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        Notification saved = notificationRepository.save(n);
        sendEmailAsync(user.getEmail(), title, message);
        return saved;
    }

    @Async
    public void sendEmailAsync(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("PayFlow: " + subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // DEMO: mail server likely isn't configured; log instead of failing the request.
            log.warn("Could not send email notification to {}: {}", toEmail, e.getMessage());
        }
    }

    public List<NotificationResponse> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getType().name())
                        .read(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .toList();
    }

    public long unreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    public void markAllRead(User user) {
        List<Notification> unread = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
