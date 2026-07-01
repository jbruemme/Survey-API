package org.example.ser421lab6.dto.login;

public record AuthResponseDto(
        String token,
        UserDto user
) {}
