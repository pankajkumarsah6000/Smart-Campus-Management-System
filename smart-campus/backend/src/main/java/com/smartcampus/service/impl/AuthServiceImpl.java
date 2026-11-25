package com.smartcampus.service.impl;

import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.dto.request.RefreshTokenRequest;
import com.smartcampus.dto.request.StudentRegistrationRequest;
import com.smartcampus.dto.response.AuthResponse;
import com.smartcampus.entity.Role;
import com.smartcampus.entity.RoleName;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.User;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.repository.RoleRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.security.JwtUtil;
import com.smartcampus.security.UserDetailsServiceImpl;
import com.smartcampus.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        return createAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse registerStudent(StudentRegistrationRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("A user with this email already exists");
        }
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadRequestException("Password must be no more than 72 UTF-8 bytes");
        }

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.STUDENT);
                    return roleRepository.save(role);
                });

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEnabled(true);
        user.setRoles(new HashSet<>(List.of(studentRole)));
        user = userRepository.save(user);

        Student student = new Student();
        student.setUser(user);
        student.setRollNumber(generateRollNumber());
        studentRepository.save(student);

        return createAuthResponse(user);
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

    private AuthResponse createAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), roles);
    }

    private String generateRollNumber() {
        String rollNumber;
        do {
            rollNumber = "STU-" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 26).toUpperCase(Locale.ROOT);
        } while (studentRepository.findByRollNumber(rollNumber).isPresent());
        return rollNumber;
    }
}
