package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import com.patitasalrescate.data.source.IAuthApiDataSource;

public final class AuthApiDataSource implements IAuthApiDataSource {
    private final AuthApiService service;

    public AuthApiDataSource(retrofit2.Retrofit retrofit) {
        service = retrofit.create(AuthApiService.class);
    }

    @Override public Call<AuthResponse> login(LoginRequest body) {
        java.util.Objects.requireNonNull(body, "body");
        return service.login(body);
    }

    @Override public Call<AuthResponse> register(RegisterRequest body) {
        java.util.Objects.requireNonNull(body, "body");
        FormParts parts = new FormParts();
        parts.field("firstName", body.firstName);
        parts.field("lastName", body.lastName);
        parts.field("email", body.email);
        parts.field("password", body.password);
        parts.field("birthDate", body.birthDate);
        parts.field("gender", body.gender);
        parts.file("photo", body.photo);
        return service.register(parts.build());
    }

    @Override public Call<Void> verifyEmail(String userId, String token) {
        java.util.Objects.requireNonNull(userId, "userId");
        java.util.Objects.requireNonNull(token, "token");
        return service.verifyEmail(userId, token);
    }

    @Override public Call<Void> sendVerificationEmail() {
        return service.sendVerificationEmail();
    }
}
