package com.auth.auth_microservice.controller;

import com.auth.auth_microservice.model.AuthRequest;
import com.auth.auth_microservice.model.User;
import com.auth.auth_microservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@Valid @RequestBody AuthRequest authRequest) {
        return RxJava3Adapter.singleToMono(authService.authenticate(authRequest)
                .map(token -> ResponseEntity.ok().body(Map.of("token", token))));
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<User>> register(@Valid @RequestBody User user) {
        return RxJava3Adapter.singleToMono(authService.register(user))
                .map(userResponse -> ResponseEntity.ok().body(userResponse));
    }

    @GetMapping
    public Flux<User> getAll() {
        return RxJava3Adapter.flowableToFlux(authService.getAll());
    }
}
