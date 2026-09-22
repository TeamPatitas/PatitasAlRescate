package com.patitasalrescate.data.repository;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import com.patitasalrescate.data.source.IAdminApiDataSource;

/** API repository, injectable independently of the existing local repositories. */
public final class AdminApiRepository {
    private final IAdminApiDataSource source;

    public AdminApiRepository(IAdminApiDataSource source) {
        this.source = java.util.Objects.requireNonNull(source);
    }

    public Call<HealthResponse> healthCheck() {
        return source.healthCheck();
    }

    public Call<UserResponse> getCurrentUser() {
        return source.getCurrentUser();
    }

    public Call<UserResponse> updateCurrentUser(UpdateUserRequest body) {
        return source.updateCurrentUser(body);
    }

    public Call<Boolean> deleteUser(String id) {
        return source.deleteUser(id);
    }

    public Call<Void> addRoles(SwitchRolesRequest body) {
        return source.addRoles(body);
    }

    public Call<Void> removeRoles(SwitchRolesRequest body) {
        return source.removeRoles(body);
    }

    public Call<UserSummaryResponsePagedResponse> getAllUsers(Integer page, Integer pageSize) {
        return source.getAllUsers(page, pageSize);
    }

    public Call<ShelterResponse> enableShelter(String id) {
        return source.enableShelter(id);
    }

    public Call<ShelterResponse> disableShelter(String id) {
        return source.disableShelter(id);
    }
}
