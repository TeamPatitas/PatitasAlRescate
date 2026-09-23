package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import com.patitasalrescate.data.source.IAdminApiDataSource;

public final class AdminApiDataSource implements IAdminApiDataSource {
    private final AdminApiService service;

    public AdminApiDataSource(retrofit2.Retrofit retrofit) {
        service = retrofit.create(AdminApiService.class);
    }

    @Override public Call<HealthResponse> healthCheck() {
        return service.healthCheck();
    }

    @Override public Call<UserResponse> getCurrentUser() {
        return service.getCurrentUser();
    }

    @Override public Call<UserResponse> updateCurrentUser(UpdateUserRequest body) {
        java.util.Objects.requireNonNull(body, "body");
        FormParts parts = new FormParts();
        parts.field("firstName", body.firstName);
        parts.field("lastName", body.lastName);
        parts.field("birthDate", body.birthDate);
        parts.field("gender", body.gender);
        parts.file("photo", body.photo);
        return service.updateCurrentUser(parts.build());
    }

    @Override public Call<Boolean> deleteUser(String id) {
        java.util.Objects.requireNonNull(id, "id");
        return service.deleteUser(id);
    }

    @Override public Call<Void> addRoles(SwitchRolesRequest body) {
        java.util.Objects.requireNonNull(body, "body");
        return service.addRoles(body);
    }

    @Override public Call<Void> removeRoles(SwitchRolesRequest body) {
        java.util.Objects.requireNonNull(body, "body");
        return service.removeRoles(body);
    }

    @Override public Call<UserSummaryResponsePagedResponse> getAllUsers(Integer page, Integer pageSize) {
        if (page != null && page < 1) throw new IllegalArgumentException("page must be positive");
        if (pageSize != null && pageSize < 1) throw new IllegalArgumentException("pageSize must be positive");
        return service.getAllUsers(page, pageSize);
    }

    @Override public Call<ShelterResponse> enableShelter(String id) {
        java.util.Objects.requireNonNull(id, "id");
        return service.enableShelter(id);
    }

    @Override public Call<ShelterResponse> disableShelter(String id) {
        java.util.Objects.requireNonNull(id, "id");
        return service.disableShelter(id);
    }
}
