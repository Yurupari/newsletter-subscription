package com.yurupari.notification_service.service.impl;

import com.yurupari.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${email.from}")
    private String emailFrom;

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email: to={}, subject={}", to, subject);

        var message = buildMessage(to, subject, body);

        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            log.error("Failed to send email: to={}", to, e);
        }
    }

    private SimpleMailMessage buildMessage(
            String to,
            String subject,
            String body
    ) {
        var message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(emailFrom);
        message.setSubject(subject);
        message.setText(body);

        return message;
    }
}
