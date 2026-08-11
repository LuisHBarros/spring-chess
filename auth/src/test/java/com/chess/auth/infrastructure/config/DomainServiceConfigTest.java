package com.chess.auth.infrastructure.config;

import com.chess.auth.domain.port.EmailService;
import com.chess.auth.domain.port.PasswordEncoder;
import com.chess.auth.domain.port.PasswordRecoveryTokenService;
import com.chess.auth.domain.repository.UserRepository;
import com.chess.auth.domain.service.PasswordRecoveryService;
import com.chess.auth.domain.service.UserLoginService;
import com.chess.auth.domain.service.UserRegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DomainServiceConfigTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordRecoveryTokenService tokenService;

    private final DomainServiceConfig config = new DomainServiceConfig();

    @Test
    @DisplayName("Should create domain service beans")
    void shouldCreateDomainServiceBeans() {
        UserRegistrationService registrationService = config.userRegistrationService(userRepository, passwordEncoder);
        assertNotNull(registrationService);

        UserLoginService loginService = config.userLoginService(userRepository, passwordEncoder);
        assertNotNull(loginService);

        PasswordRecoveryService recoveryService = config.passwordRecoveryService(userRepository, emailService, tokenService, passwordEncoder);
        assertNotNull(recoveryService);
    }
}
