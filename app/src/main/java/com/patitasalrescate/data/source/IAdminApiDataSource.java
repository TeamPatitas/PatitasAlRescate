package com.patitasalrescate.data.source;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;

/** Remote operations; enqueue calls off the UI thread. */
public interface IAdminApiDataSource {
    Call<HealthResponse> healthCheck();
    Call<UserResponse> getCurrentUser();
    Call<UserResponse> updateCurrentUser(UpdateUserRequest body);
    Call<Boolean> deleteUser(String id);
    Call<Void> addRoles(SwitchRolesRequest body);
    Call<Void> removeRoles(SwitchRolesRequest body);
    Call<UserSummaryResponsePagedResponse> getAllUsers(Integer page, Integer pageSize);
    Call<ShelterResponse> enableShelter(String id);
    Call<ShelterResponse> disableShelter(String id);
}
