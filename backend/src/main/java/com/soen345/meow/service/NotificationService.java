package com.soen345.meow.service;

import com.soen345.meow.entity.NotificationLog;
import com.soen345.meow.repository.NotificationLogRepository;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final JavaMailSender javaMailSender;
    private final NotificationLogRepository notificationLogRepository;

    public NotificationService(JavaMailSender javaMailSender, NotificationLogRepository notificationLogRepository) {
        this.javaMailSender = javaMailSender;
        this.notificationLogRepository = notificationLogRepository;
    }

    public void sendBookingConfirmation(String toEmail, String eventTitle, Long reservationId, LocalDateTime eventDate) {
        if (!StringUtils.hasText(toEmail)) {
            return;
        }

        String subject = "Booking Confirmed - " + eventTitle;
        String html = "<h2>Reservation Confirmed</h2>"
                + "<p>Event: <b>" + eventTitle + "</b></p>"
                + "<p>Reservation ID: <b>" + reservationId + "</b></p>"
                + "<p>Event Date: <b>" + (eventDate == null ? "TBD" : eventDate) + "</b></p>";

        sendEmailAndLog(toEmail, subject, html, eventTitle, "BOOKING_CONFIRMATION");
    }

    public void sendEventCancellationNotice(String toEmail, String eventTitle) {
        if (!StringUtils.hasText(toEmail)) {
            return;
        }

        String subject = "Event Cancelled - " + eventTitle;
        String html = "<h2>Event Cancellation Notice</h2>"
                + "<p>Your booked event <b>" + eventTitle + "</b> has been cancelled.</p>";

        sendEmailAndLog(toEmail, subject, html, eventTitle, "EVENT_CANCELLATION");
    }

    private void sendEmailAndLog(String toEmail, String subject, String html, String eventTitle, String messageType) {
        NotificationLog log = new NotificationLog();
        log.setChannel("EMAIL");
        log.setMessageType(messageType);
        log.setRecipient(toEmail);
        log.setEventTitle(eventTitle);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            javaMailSender.send(message);
            log.setDeliveryStatus("SENT");
        } catch (MailException | jakarta.mail.MessagingException ex) {
            log.setDeliveryStatus("FAILED");
        }

        notificationLogRepository.save(log);
    }
}
