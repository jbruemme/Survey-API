package org.example.ser421lab6.service;

import lombok.RequiredArgsConstructor;
import org.example.ser421lab6.dto.login.AuthResponseDto;
import org.example.ser421lab6.dto.login.LoginRequestDto;
import org.example.ser421lab6.dto.login.RegisterRequestDto;
import org.example.ser421lab6.dto.login.UserDto;
import org.example.ser421lab6.entity.UserEntity;
import org.example.ser421lab6.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponseDto register(RegisterRequestDto request) {
        String email = request.email().trim().toLowerCase();

        // Validating request
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // Creating requested user
        UserEntity user = new UserEntity().builder()
                .displayName(request.displayName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        // Saving new user
        UserEntity savedUser = userRepository.save(user);

        // Generating JWT token
        String token = jwtService.generateToken(savedUser);

        return new AuthResponseDto(token, toUserDto(savedUser));
    }

    /**
     * Function to authorize user login
     * @param request User request with login information
     * @return AuthResponseDto response
     */
    public AuthResponseDto login(LoginRequestDto request) {
        String email = request.email().trim().toLowerCase();

        // Verifying user email exists
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        // Verifying password matches
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Generating token
        String token = jwtService.generateToken(user);

        return new AuthResponseDto(token, toUserDto(user));
    }

    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserEntity user)) {
            throw new IllegalArgumentException("User is not authenticated.");
        }

        return toUserDto(user);
    }

    /**
     * Helper method for mapping user date to the UserDto
     * @param user The user data to be mapped
     * @return The created UserDto
     */
    private UserDto toUserDto(UserEntity user) {
        return new UserDto(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
