package com.example.userenrollment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailSender implements EmailSender {
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailSender(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendVerificationPasscode(String recipient, String passcode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject("MovieKnight verification code");
        message.setText("Your MovieKnight verification code is " + passcode
                + ". It expires in 5 minutes. If you did not request this, ignore this email.");
        mailSender.send(message);
    }
}
