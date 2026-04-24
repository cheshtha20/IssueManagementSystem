package com.issuemanage.auth.service.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@ims.com}")
    private String fromEmail;

    public EmailNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    @org.springframework.scheduling.annotation.Async
    public void sendOtp(String toEmail, String subject, String body, String otp) {
        if (mailSender == null) {
            LOGGER.info("JavaMailSender is not configured. OTP for {} is {}", toEmail, otp);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            LOGGER.warn("Email delivery failed for {}. Falling back to application logs. Reason: {}", toEmail, ex.getMessage());
            LOGGER.info("OTP for {} is {}", toEmail, otp);
        }
    }
}
