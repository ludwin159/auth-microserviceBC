package com.auth.auth_microservice.service.serviceImp;

import com.auth.auth_microservice.exceptions.ClientNotFound;
import com.auth.auth_microservice.exceptions.InvalidCredentials;
import com.auth.auth_microservice.model.AuthRequest;
import com.auth.auth_microservice.model.User;
import com.auth.auth_microservice.repository.UserRepository;
import com.auth.auth_microservice.service.AuthService;
import com.auth.auth_microservice.security.JwtUtil;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Service
@Slf4j
public class AuthServiceImp implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    public AuthServiceImp(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    @Override
    public Single<String> authenticate(AuthRequest authRequest) {
        return RxJava3Adapter.monoToSingle(
                userRepository.findByUsername(authRequest.getUsername())
                        .switchIfEmpty(
                            Mono.error(
                                new ClientNotFound("The client with id "+authRequest.getUsername()+" not exists")))
                        .flatMap(user -> {
                            if (passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
                                String token = jwtUtil.generateToken(user.getUsername());
                                return Mono.just(token);
                            }
                            return Mono.error(new InvalidCredentials("The credentials are incorrect"));
                        })
        );
    }

    @Override
    public Single<User> register(User authRequest) {
        return RxJava3Adapter.monoToSingle(
                userRepository.findByUsername(authRequest.getUsername())
                        .flatMap(user -> Mono.error(new InvalidCredentials("User already exist.")))
                        .switchIfEmpty(userRepository.save(
                                User.builder()
                                        .id(UUID.randomUUID().toString())
                                        .username(authRequest.getUsername())
                                        .password(passwordEncoder.encode(authRequest.getPassword()))
                                        .email(authRequest.getEmail())
                                        .dateBorn(authRequest.getDateBorn()).build()))
                        .cast(User.class));
    }

    @Override
    public Flowable<User> getAll() {
        return RxJava3Adapter.fluxToFlowable(userRepository.findAll());
    }


}
