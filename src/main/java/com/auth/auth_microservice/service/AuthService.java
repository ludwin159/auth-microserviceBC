package com.auth.auth_microservice.service;

import com.auth.auth_microservice.model.AuthRequest;
import com.auth.auth_microservice.model.User;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface AuthService {
    Single<String> authenticate(AuthRequest authRequest);

    Single<User> register(User authRequest);
    Flowable<User> getAll();
}
