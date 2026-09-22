package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import retrofit2.http.*;

interface AuthApiService {
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest body);

    @Multipart
    @POST("auth/register")
    Call<AuthResponse> register(@Part java.util.List<okhttp3.MultipartBody.Part> parts);

    @GET("auth/verify-email")
    Call<Void> verifyEmail(@Query("userId") String userId, @Query("token") String token);

    @GET("auth/send-verification-email")
    Call<Void> sendVerificationEmail();
}
