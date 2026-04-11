package com.soen345.meow;

import com.soen345.meow.entity.NotificationLog;
import com.soen345.meow.repository.NotificationLogRepository;
import com.soen345.meow.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldSendBookingConfirmationEmail() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        notificationService.sendBookingConfirmation(
                "customer@example.com",
                "Jazz Night",
                101L,
                LocalDateTime.of(2026, 5, 10, 20, 0)
        );

        verify(javaMailSender).send(any(MimeMessage.class));

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getMessageType()).isEqualTo("BOOKING_CONFIRMATION");
        assertThat(logCaptor.getValue().getDeliveryStatus()).isEqualTo("SENT");
        assertThat(logCaptor.getValue().getChannel()).isEqualTo("EMAIL");
        assertThat(logCaptor.getValue().getRecipient()).isEqualTo("customer@example.com");
    }

    @Test
    void shouldSendCancellationEmail() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        notificationService.sendEventCancellationNotice("customer@example.com", "Jazz Night");

        verify(javaMailSender).send(any(MimeMessage.class));

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getMessageType()).isEqualTo("EVENT_CANCELLATION");
        assertThat(logCaptor.getValue().getDeliveryStatus()).isEqualTo("SENT");
        assertThat(logCaptor.getValue().getChannel()).isEqualTo("EMAIL");
    }

    @Test
    void shouldLogFailedBookingConfirmationWhenMailSendFails() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailException("smtp down") {}).when(javaMailSender).send(any(MimeMessage.class));

        notificationService.sendBookingConfirmation(
                "customer@example.com",
                "Jazz Night",
                201L,
                LocalDateTime.of(2026, 5, 10, 20, 0)
        );

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getMessageType()).isEqualTo("BOOKING_CONFIRMATION");
        assertThat(logCaptor.getValue().getDeliveryStatus()).isEqualTo("FAILED");
    }
}
