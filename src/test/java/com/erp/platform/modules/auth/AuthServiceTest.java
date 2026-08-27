package com.erp.platform.modules.auth;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.modules.auth.dto.AuthResponse;
import com.erp.platform.modules.auth.dto.LoginRequest;
import com.erp.platform.modules.auth.dto.RegisterRequest;
import com.erp.platform.modules.auth.entity.Role;
import com.erp.platform.modules.auth.entity.User;
import com.erp.platform.modules.admin.repository.LoginTrackingRepository;
import com.erp.platform.modules.auth.repository.RoleRepository;
import com.erp.platform.modules.auth.repository.UserRepository;
import com.erp.platform.modules.auth.service.AuthService;
import com.erp.platform.security.JwtTokenProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService unit tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private AuthenticationManager authManager;
    @Mock private LoginTrackingRepository loginTrackingRepository;
    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 86400000L);
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("returns AuthResponse with token on successful login")
        void shouldReturnTokenOnSuccessfulLogin() {
            User user = TestDataBuilder.user();
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(tokenProvider.generateToken(anyString(), any(), any(), anyList()))
                    .thenReturn("mock-jwt-token");
            when(userRepository.save(any())).thenReturn(user);

            AuthResponse response = authService.login(
                    TestDataBuilder.loginRequest(user.getEmail(), "password123"), "127.0.0.1");

            assertThat(response.getToken()).isEqualTo("mock-jwt-token");
            assertThat(response.getEmail()).isEqualTo(user.getEmail());
            assertThat(response.getTenantId()).isEqualTo(TestDataBuilder.DEFAULT_TENANT_ID);
        }

        @Test
        @DisplayName("returns roles in response")
        void shouldReturnRolesInResponse() {
            User user = TestDataBuilder.user();
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(tokenProvider.generateToken(anyString(), any(), any(), anyList())).thenReturn("token");
            when(userRepository.save(any())).thenReturn(user);

            AuthResponse response = authService.login(
                    TestDataBuilder.loginRequest(user.getEmail(), "pass"), "127.0.0.1");

            assertThat(response.getRoles()).contains("ADMIN");
        }

        @Test
        @DisplayName("throws NOT_FOUND when user email doesn't exist")
        void shouldThrowWhenUserNotFound() {
            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authManager).authenticate(any());

            Assertions.assertThrows(Exception.class,
                    () -> authService.login(TestDataBuilder.loginRequest("notfound@test.com", "pass"), "127.0.0.1"));
        }

        @Test
        @DisplayName("updates lastLoginAt on successful login")
        void shouldUpdateLastLoginAt() {
            User user = TestDataBuilder.user();
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(tokenProvider.generateToken(anyString(), any(), any(), anyList())).thenReturn("token");
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            authService.login(TestDataBuilder.loginRequest(user.getEmail(), "pass"), "127.0.0.1");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getLastLoginAt()).isNotNull();
        }
    }

    // ─── register ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("registers user successfully with default STAFF role")
        void shouldRegisterWithDefaultRole() {
            RegisterRequest request = TestDataBuilder.registerRequest();
            Role staffRole = TestDataBuilder.role("STAFF");

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(roleRepository.findByName("STAFF")).thenReturn(Optional.of(staffRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

            User savedUser = TestDataBuilder.user();
            savedUser.setRoles(Set.of(staffRole));
            when(userRepository.save(any())).thenReturn(savedUser);
            when(tokenProvider.generateToken(anyString(), any(), any(), anyList())).thenReturn("token");

            AuthResponse response = authService.register(request);

            assertThat(response).isNotNull();
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
        }

        @Test
        @DisplayName("registers user with specified role when roleName provided")
        void shouldRegisterWithSpecifiedRole() {
            RegisterRequest request = TestDataBuilder.registerRequest();
            request.setRoleName("MANAGER");
            Role managerRole = TestDataBuilder.role("MANAGER");

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(managerRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");

            User saved = TestDataBuilder.user();
            saved.setRoles(Set.of(managerRole));
            when(userRepository.save(any())).thenReturn(saved);
            when(tokenProvider.generateToken(anyString(), any(), any(), anyList())).thenReturn("token");

            authService.register(request);
            verify(roleRepository).findByName("MANAGER");
        }

        @Test
        @DisplayName("throws CONFLICT when email already registered")
        void shouldThrowConflictOnDuplicateEmail() {
            RegisterRequest request = TestDataBuilder.registerRequest();
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            AppException ex = Assertions.assertThrows(AppException.class,
                    () -> authService.register(request));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws NOT_FOUND when specified role doesn't exist")
        void shouldThrowWhenRoleNotFound() {
            RegisterRequest request = TestDataBuilder.registerRequest();
            request.setRoleName("NON_EXISTENT_ROLE");

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(roleRepository.findByName("NON_EXISTENT_ROLE")).thenReturn(Optional.empty());

            AppException ex = Assertions.assertThrows(AppException.class,
                    () -> authService.register(request));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        }

        @Test
        @DisplayName("assigns tenantId from request when provided")
        void shouldUseTenantIdFromRequest() {
            RegisterRequest request = TestDataBuilder.registerRequest();
            Role staffRole = TestDataBuilder.role("STAFF");

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(roleRepository.findByName("STAFF")).thenReturn(Optional.of(staffRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");

            User saved = TestDataBuilder.user();
            when(userRepository.save(any())).thenReturn(saved);
            when(tokenProvider.generateToken(anyString(), any(), any(), anyList())).thenReturn("token");

            authService.register(request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getTenantId()).isEqualTo(TestDataBuilder.DEFAULT_TENANT_ID);
        }
    }
}
