package com.chess.auth.infrastructure.config;

import com.chess.auth.domain.port.EmailService;
import com.chess.auth.domain.port.PasswordEncoder;
import com.chess.auth.domain.port.PasswordRecoveryTokenService;
import com.chess.auth.domain.port.TokenBlacklistService;
import com.chess.auth.domain.port.TokenProvider;
import com.chess.auth.domain.repository.UserRepository;
import com.chess.auth.domain.service.PasswordRecoveryService;
import com.chess.auth.domain.service.TokenAuthenticationService;
import com.chess.auth.domain.service.UserLoginService;
import com.chess.auth.domain.service.UserRegistrationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public UserRegistrationService userRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new UserRegistrationService(userRepository, passwordEncoder);
    }

    @Bean
    public UserLoginService userLoginService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new UserLoginService(userRepository, passwordEncoder);
    }

    @Bean
    public PasswordRecoveryService passwordRecoveryService(UserRepository userRepository,
                                                         EmailService emailService,
                                                         PasswordRecoveryTokenService tokenService,
                                                         PasswordEncoder passwordEncoder) {
        return new PasswordRecoveryService(userRepository, emailService, tokenService, passwordEncoder);
    }

    @Bean
    public TokenAuthenticationService tokenAuthenticationService(TokenProvider tokenProvider,
                                                                 TokenBlacklistService blacklistService,
                                                                 UserRepository userRepository) {
        return new TokenAuthenticationService(tokenProvider, blacklistService, userRepository);
    }
}
