package org.example.ser421lab6.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.ser421lab6.entity.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Method to generate jwt token for user
     * @param user The user requesting the token
     * @return The generated token with user email, ID, role, token issue time, expiration time, and signing key
     */
    public String generateToken(UserEntity user) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Helper method to extract user email from the token
     * @param token The generated jwt token
     * @return The token's email
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Helper method to extract userId from the token
     * @param token The generated jwt token
     * @return The token's userId
     */
    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");

        // Type conversion
        if (userId instanceof Integer id) {
            return id.longValue();
        }
        if (userId instanceof Long id) {
            return id;
        }
        return Long.valueOf(userId.toString());
    }

    /**
     * Method to check if the token is valid, checks if token email is same as user's email and if the token is expired
     * @param token The jwt token being checked
     * @param user The user using the token
     * @return True if same email and token not expired otherwise false
     */
    public boolean isTokenValid(String token, UserEntity user) {
        String email = extractEmail(token);
        return email.equals(user.getEmail()) && !isTokenExpired(token);
    }

    /**
     * Helper method to check if toke is expired
     * @param token The jwt token being checked
     * @return True if expiration date is before current date otherwise false
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    /**
     * Helper method to parse claims from Jwt token
     * @param token The token to be parsed
     * @return Parsed Jwt object
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Helper method for retrieving the signing key
     * @return Signing key
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

}
