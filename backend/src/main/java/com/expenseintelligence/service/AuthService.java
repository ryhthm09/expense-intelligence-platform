package com.expenseintelligence.service;

import com.expenseintelligence.config.JwtProperties;
import com.expenseintelligence.domain.entity.User;
import com.expenseintelligence.dto.request.LoginRequest;
import com.expenseintelligence.dto.request.RegisterRequest;
import com.expenseintelligence.dto.response.AuthResponse;
import com.expenseintelligence.dto.response.UserResponse;
import com.expenseintelligence.exception.BadRequestException;
import com.expenseintelligence.mapper.UserMapper;
import com.expenseintelligence.repository.UserRepository;
import com.expenseintelligence.security.JwtTokenProvider;
import com.expenseintelligence.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .build();

        user = userRepository.save(user);
        return buildAuthResponse(new UserPrincipal(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        return buildAuthResponse(new UserPrincipal(user));
    }

    private AuthResponse buildAuthResponse(UserPrincipal principal) {
        String token = jwtTokenProvider.generateToken(principal);
        UserResponse userResponse = userMapper.toResponse(
                userRepository.findById(principal.getId()).orElseThrow());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpirationMs())
                .user(userResponse)
                .build();
    }
}
