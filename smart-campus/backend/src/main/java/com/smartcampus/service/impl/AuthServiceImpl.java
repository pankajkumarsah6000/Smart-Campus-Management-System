package com.smartcampus.service.impl;

import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.dto.request.RefreshTokenRequest;
import com.smartcampus.dto.response.AuthResponse;
import com.smartcampus.entity.User;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.security.JwtUtil;
import com.smartcampus.security.UserDetailsServiceImpl;
import com.smartcampus.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), roles);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtUtil.isRefreshToken(token)) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        String username = jwtUtil.extractUsername(token);

        if (!jwtUtil.isTokenValid(token, username)) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return new AuthResponse(newAccessToken, newRefreshToken, user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), roles);
    }
}
