package com.chess.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for user login. Provide either email or username along with password.")
public class LoginRequestDto {

    @Schema(description = "User email address (provide email or username)", example = "player@chess.com")
    private String email;

    @Schema(description = "Username (provide email or username)", example = "chess_master")
    private String username;

    @Schema(description = "User password", example = "Str0ngP@ss!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequestDto() {}

    public LoginRequestDto(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
