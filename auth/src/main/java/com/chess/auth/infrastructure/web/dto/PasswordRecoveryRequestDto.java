package com.chess.auth.infrastructure.web.dto;

public class PasswordRecoveryRequestDto {
    private String email;

    public PasswordRecoveryRequestDto() {}

    public PasswordRecoveryRequestDto(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
