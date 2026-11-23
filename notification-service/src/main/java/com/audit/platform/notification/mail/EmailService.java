package com.audit.platform.notification.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public boolean sendEmail(String from, String to, String subject, String content) {
        log.info("Sending audit alert email from '{}' to '{}' [Subject: '{}']", from, to, subject);

        if (mailSender == null) {
            log.info("JavaMailSender not available. Simulating email dispatch to MailHog: SUCCESS");
            return true;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("Email successfully dispatched to SMTP server (MailHog)");
            return true;
        } catch (Exception e) {
            log.warn("SMTP dispatch failed (falling back to simulated log): {}", e.getMessage());
            return false;
        }
    }
}

