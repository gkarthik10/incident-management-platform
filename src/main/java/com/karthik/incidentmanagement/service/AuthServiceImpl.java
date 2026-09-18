package com.karthik.incidentmanagement.service;

import com.karthik.incidentmanagement.dto.IncidentMapper;
import com.karthik.incidentmanagement.dto.LoginRequestDto;
import com.karthik.incidentmanagement.dto.LoginResponseDto;
import com.karthik.incidentmanagement.dto.RegisterRequestDto;
import com.karthik.incidentmanagement.dto.UserSummaryDto;
import com.karthik.incidentmanagement.entity.User;
import com.karthik.incidentmanagement.exception.DuplicateResourceException;
import com.karthik.incidentmanagement.exception.InvalidCredentialsException;
import com.karthik.incidentmanagement.exception.ResourceNotFoundException;
import com.karthik.incidentmanagement.repository.UserRepository;
import com.karthik.incidentmanagement.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public String register(RegisterRequestDto dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            // A 409 Conflict, not a silently-returned "already exists" string,
            // so API clients can actually branch on the outcome.
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();

        userRepository.save(user);

        return "User registered successfully";
    }

    @Override
    public LoginResponseDto login(LoginRequestDto dto) {

        // Deliberately use the SAME exception and message whether the email
        // doesn't exist or the password is wrong, so the API never reveals
        // which registered emails exist (a previous version of this method
        // leaked a "not found" 404 with the message "Incident not found",
        // which was both a security smell and a copy-paste bug).
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return new LoginResponseDto(jwtService.generateToken(user.getEmail()));
    }

    @Override
    public UserSummaryDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return IncidentMapper.toUserSummary(user);
    }
}
