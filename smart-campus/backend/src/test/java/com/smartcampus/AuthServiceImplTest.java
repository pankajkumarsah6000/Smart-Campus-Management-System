package com.smartcampus;

import com.smartcampus.dto.request.StudentRegistrationRequest;
import com.smartcampus.entity.Role;
import com.smartcampus.entity.RoleName;
import com.smartcampus.repository.RoleRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.JwtUtil;
import com.smartcampus.security.UserDetailsServiceImpl;
import com.smartcampus.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    @Test
    void studentRegistrationCreatesMissingStudentRole() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        UserRepository userRepository = mock(UserRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        StudentRepository studentRepository = mock(StudentRepository.class);
        UserDetailsServiceImpl userDetailsService = mock(UserDetailsServiceImpl.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        Role studentRole = new Role();
        studentRole.setName(RoleName.STUDENT);

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(roleRepository.findByName(RoleName.STUDENT)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(studentRole);
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(studentRepository.findByRollNumber(any())).thenReturn(Optional.empty());
        when(jwtUtil.generateAccessToken(any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");

        AuthServiceImpl service = new AuthServiceImpl(
                authenticationManager,
                userRepository,
                roleRepository,
                studentRepository,
                userDetailsService,
                jwtUtil,
                passwordEncoder
        );

        StudentRegistrationRequest request = new StudentRegistrationRequest();
        request.setFirstName("New");
        request.setLastName("Student");
        request.setEmail("new.student@smartcampus.edu");
        request.setPassword("StudentSignup@123");

        var response = service.registerStudent(request);

        verify(roleRepository).save(any(Role.class));
        assertThat(response.getRoles()).containsExactly("STUDENT");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }
}
