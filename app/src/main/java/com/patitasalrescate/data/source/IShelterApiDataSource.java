package com.patitasalrescate.data.source;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;

/** Remote operations; enqueue calls off the UI thread. */
public interface IShelterApiDataSource {
    Call<ShelterResponse> createShelter(CreateShelterRequest body);
    Call<ShelterSummaryResponsePagedResponse> getAllShelters(Integer page, Integer pageSize);
    Call<ShelterResponse> updateShelter(String id, UpdateShelterRequest body);
    Call<ShelterResponse> getShelterById(String id);
    Call<Void> deleteShelter(String id);
}
