package com.patitasalrescate.data.repository;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import com.patitasalrescate.data.source.IShelterApiDataSource;

/** API repository, injectable independently of the existing local repositories. */
public final class ShelterApiRepository {
    private final IShelterApiDataSource source;

    public ShelterApiRepository(IShelterApiDataSource source) {
        this.source = java.util.Objects.requireNonNull(source);
    }

    public Call<ShelterResponse> createShelter(CreateShelterRequest body) {
        return source.createShelter(body);
    }

    public Call<ShelterSummaryResponsePagedResponse> getAllShelters(Integer page, Integer pageSize) {
        return source.getAllShelters(page, pageSize);
    }

    public Call<ShelterResponse> updateShelter(String id, UpdateShelterRequest body) {
        return source.updateShelter(id, body);
    }

    public Call<ShelterResponse> getShelterById(String id) {
        return source.getShelterById(id);
    }

    public Call<Void> deleteShelter(String id) {
        return source.deleteShelter(id);
    }
}
