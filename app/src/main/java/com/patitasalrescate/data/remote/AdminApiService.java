package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import retrofit2.http.*;

interface AdminApiService {
    @GET("admin/health")
    Call<HealthResponse> healthCheck();

    @GET("admin/user")
    Call<UserResponse> getCurrentUser();

    @Multipart
    @PATCH("admin/user")
    Call<UserResponse> updateCurrentUser(@Part java.util.List<okhttp3.MultipartBody.Part> parts);

    @DELETE("admin/user/{id}")
    Call<Boolean> deleteUser(@Path("id") String id);

    @PATCH("admin/add-roles")
    Call<Void> addRoles(@Body SwitchRolesRequest body);

    @PATCH("admin/remove-roles")
    Call<Void> removeRoles(@Body SwitchRolesRequest body);

    @GET("admin/users")
    Call<UserSummaryResponsePagedResponse> getAllUsers(@Query("page") Integer page, @Query("pageSize") Integer pageSize);

    @PATCH("admin/shelter/enable/{id}")
    Call<ShelterResponse> enableShelter(@Path("id") String id);

    @PATCH("admin/shelter/disable/{id}")
    Call<ShelterResponse> disableShelter(@Path("id") String id);
}
