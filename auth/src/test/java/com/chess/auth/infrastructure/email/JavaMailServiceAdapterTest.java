package com.chess.auth.infrastructure.email;

import com.chess.auth.domain.model.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JavaMailServiceAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    @DisplayName("Should send recovery email with correct parameters")
    void shouldSendRecoveryEmail() {
        JavaMailServiceAdapter mailAdapter = new JavaMailServiceAdapter(mailSender);
        Email recipient = new Email("player@chess.com");
        String token = "recovery-token-xyz";

        mailAdapter.sendPasswordRecoveryEmail(recipient, token);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertNotNull(sentMessage);
        assertArrayEquals(new String[]{"player@chess.com"}, sentMessage.getTo());
        assertEquals("Password Recovery Request", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(token));
    }
}
