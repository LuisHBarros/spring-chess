package com.chess.auth.infrastructure.email;

import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.port.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class JavaMailServiceAdapter implements EmailService {

    private final JavaMailSender mailSender;

    public JavaMailServiceAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordRecoveryEmail(Email recipient, String recoveryToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@chess.com");
        message.setTo(recipient.getValue());
        message.setSubject("Password Recovery Request");
        message.setText("To reset your password, please use the following recovery token: " + recoveryToken);

        mailSender.send(message);
    }
}
