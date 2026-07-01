package org.example.ser421lab6.dto.login;

public record UserDto(
        Long id,
        String displayName,
        String email,
        String role
) {}
