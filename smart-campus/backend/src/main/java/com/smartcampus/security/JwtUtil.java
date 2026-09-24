package com.smartcampus.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        // Secret must be at least 256 bits for HS256. Configure a strong value via JWT_SECRET env var.
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getId())
                .claim("roles", roles)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(CustomUserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getId())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number num) {
            return num.longValue();
        }
        throw new io.jsonwebtoken.JwtException("Token does not contain a userId claim");
    }

    /** Returns the token's type claim ("access" or "refresh"). */
    public String extractType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    /**
     * True when the token is explicitly a refresh token (has type=refresh).
     * This prevents an access token from being replayed on the /auth/refresh endpoint.
     */
    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(extractType(token));
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
