package com.patitasalrescate.data.repository;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import com.patitasalrescate.data.source.IAuthApiDataSource;

/** API repository, injectable independently of the existing local repositories. */
public final class AuthApiRepository {
    private final IAuthApiDataSource source;

    public AuthApiRepository(IAuthApiDataSource source) {
        this.source = java.util.Objects.requireNonNull(source);
    }

    public Call<AuthResponse> login(LoginRequest body) {
        return source.login(body);
    }

    public Call<AuthResponse> register(RegisterRequest body) {
        return source.register(body);
    }

    public Call<Void> verifyEmail(String userId, String token) {
        return source.verifyEmail(userId, token);
    }

    public Call<Void> sendVerificationEmail() {
        return source.sendVerificationEmail();
    }
}
