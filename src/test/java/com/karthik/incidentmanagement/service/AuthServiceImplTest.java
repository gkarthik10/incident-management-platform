package com.karthik.incidentmanagement.service;

import com.karthik.incidentmanagement.dto.LoginRequestDto;
import com.karthik.incidentmanagement.dto.LoginResponseDto;
import com.karthik.incidentmanagement.dto.RegisterRequestDto;
import com.karthik.incidentmanagement.entity.Role;
import com.karthik.incidentmanagement.entity.User;
import com.karthik.incidentmanagement.exception.DuplicateResourceException;
import com.karthik.incidentmanagement.exception.InvalidCredentialsException;
import com.karthik.incidentmanagement.repository.UserRepository;
import com.karthik.incidentmanagement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure Mockito unit tests — no Spring context, no DB — so these run in
 * milliseconds and exercise AuthServiceImpl's actual decision logic
 * (the part that had the login bug in the original version) in isolation.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L)
                .name("Karthik")
                .email("karthik@example.com")
                .password("hashed-password")
                .role(Role.EMPLOYEE)
                .build();
    }

    @Test
    void register_savesNewUser_withEncodedPassword() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setName("New User");
        dto.setEmail("new@example.com");
        dto.setPassword("plaintext");
        dto.setRole(Role.EMPLOYEE);

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plaintext")).thenReturn("encoded-value");

        authService.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("new@example.com");
        // The raw password must never be persisted as-is.
        assertThat(saved.getPassword()).isEqualTo("encoded-value");
    }

    @Test
    void register_rejectsDuplicateEmail() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail(existingUser.getEmail());
        dto.setName("Someone");
        dto.setPassword("pw");
        dto.setRole(Role.EMPLOYEE);

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void login_succeedsAndReturnsJwt_whenCredentialsAreCorrect() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail(existingUser.getEmail());
        dto.setPassword("correct-password");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("correct-password", existingUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(existingUser.getEmail())).thenReturn("signed.jwt.token");

        LoginResponseDto response = authService.login(dto);

        assertThat(response.getToken()).isEqualTo("signed.jwt.token");
    }

    @Test
    void login_rejectsUnknownEmail_withInvalidCredentialsException() {
        // Regression test for the original bug, where a missing user threw
        // ResourceNotFoundException("Incident not found") — a 404 that both
        // used the wrong exception type and leaked account existence.
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("nobody@example.com");
        dto.setPassword("whatever");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_rejectsWrongPassword_withSameExceptionAsUnknownEmail() {
        // A wrong password and an unknown email must be indistinguishable
        // to the caller, so the API never confirms which emails exist.
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail(existingUser.getEmail());
        dto.setPassword("wrong-password");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong-password", existingUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(jwtService, org.mockito.Mockito.never()).generateToken(anyString());
    }
}
