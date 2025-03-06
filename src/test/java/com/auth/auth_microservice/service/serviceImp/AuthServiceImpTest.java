package com.auth.auth_microservice.service.serviceImp;

import com.auth.auth_microservice.exceptions.ClientNotFound;
import com.auth.auth_microservice.exceptions.InvalidCredentials;
import com.auth.auth_microservice.model.AuthRequest;
import com.auth.auth_microservice.model.User;
import com.auth.auth_microservice.repository.UserRepository;
import com.auth.auth_microservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImpTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImp authService;

    private User user1;
    private final String token = "a6s5f6as5df6as3f2as6df5a6sd5f6as2dfa6sd5fa6s5f";

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id("123")
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .dateBorn(LocalDate.now())
                .build();

    }

    @Test
    @DisplayName("Authentication success")
    void authenticateSuccess() {
        AuthRequest request = new AuthRequest("testuser", "password");
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Mono.just(user1));
        when(passwordEncoder.matches(request.getPassword(), user1.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user1.getUsername())).thenReturn(token);

        Mono<String> result = RxJava3Adapter.singleToMono(authService.authenticate(request));

        StepVerifier.create(result)
                .expectNext(token)
                .verifyComplete();

        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), user1.getPassword());
        verify(jwtUtil, times(1)).generateToken(user1.getUsername());
    }

    @Test
    @DisplayName("Authenticate when user not found")
    void authenticateUserNotFound() {
        AuthRequest request = new AuthRequest("nonexistent", "password");
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Mono.empty());

        Mono<String> result = RxJava3Adapter.singleToMono(authService.authenticate(request));

        StepVerifier.create(result)
                .expectError(ClientNotFound.class)
                .verify();

        verify(userRepository, times(1)).findByUsername(request.getUsername());
    }

    @Test
    @DisplayName("Authenticate when user have invalid credentials")
    void authenticateInvalidCredentialsTest() {
        AuthRequest request = new AuthRequest("testuser", "wrongpassword");
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Mono.just(user1));
        when(passwordEncoder.matches(request.getPassword(), user1.getPassword())).thenReturn(false);

        Mono<String> result = RxJava3Adapter.singleToMono(authService.authenticate(request));

        StepVerifier.create(result)
                .expectError(InvalidCredentials.class)
                .verify();

        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), user1.getPassword());
    }

    @Test
    @DisplayName("Register a user successfully")
    void registerSuccessTest() {
        User newUser = User.builder()
                .username("newuser")
                .password("password")
                .email("new@example.com")
                .dateBorn(LocalDate.of(1998, 6, 25))
                .build();
        when(userRepository.findByUsername(newUser.getUsername())).thenReturn(Mono.empty());
        when(passwordEncoder.encode(newUser.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user1));

        Mono<User> result = RxJava3Adapter.singleToMono(authService.register(newUser));

        StepVerifier.create(result)
                .expectNext(user1)
                .verifyComplete();

        verify(userRepository, times(1)).findByUsername(newUser.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }
/*
    @Test
    @DisplayName("Register a user when already exists")
    void registerUserAlreadyExistsTest() {
        User existingUser = User.builder().username("testuser").build();
        when(userRepository.findByUsername(existingUser.getUsername())).thenReturn(Mono.just(user1));

        Mono<User> result = RxJava3Adapter.singleToMono(authService.register(existingUser));

        StepVerifier.create(result)
                .expectError(InvalidCredentials.class)
                .verify();

        verify(userRepository, times(1)).findByUsername(existingUser.getUsername());
    }*/

    @Test
    @DisplayName("GetAll users")
    void getAllUsersSuccess() {
        when(userRepository.findAll()).thenReturn(Flux.just(user1));

        Flux<User> result = RxJava3Adapter.flowableToFlux(authService.getAll());

        StepVerifier.create(result)
                .expectNext(user1)
                .verifyComplete();

        verify(userRepository, times(1)).findAll();
    }
}
