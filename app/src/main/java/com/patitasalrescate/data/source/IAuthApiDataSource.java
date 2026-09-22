package com.patitasalrescate.data.source;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;

/** Remote operations; enqueue calls off the UI thread. */
public interface IAuthApiDataSource {
    Call<AuthResponse> login(LoginRequest body);
    Call<AuthResponse> register(RegisterRequest body);
    Call<Void> verifyEmail(String userId, String token);
    Call<Void> sendVerificationEmail();
}
