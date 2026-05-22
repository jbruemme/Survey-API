package org.example.ser421lab6.dto.login;

import jakarta.validation.constraints.*;

/**
 * DTO for user request to register as a user
 * @param displayName User's display name
 * @param email User's email
 * @param password User's password
 */
public record RegisterRequestDto(

    @NotBlank(message = "Display name is required")
    String displayName,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
            message = "Password must be at least 8 characters and include uppercase, lowercase, number, and special character"
    )
    String password

) {}
