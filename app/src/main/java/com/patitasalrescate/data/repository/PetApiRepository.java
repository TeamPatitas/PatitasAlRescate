package com.patitasalrescate.data.repository;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import com.patitasalrescate.data.source.IPetApiDataSource;

/** API repository, injectable independently of the existing local repositories. */
public final class PetApiRepository {
    private final IPetApiDataSource source;

    public PetApiRepository(IPetApiDataSource source) {
        this.source = java.util.Objects.requireNonNull(source);
    }

    public Call<PetResponse> createPet(CreatePetRequest body) {
        return source.createPet(body);
    }

    public Call<PetSummaryResponsePagedResponse> getAllPets(Integer page, Integer pageSize) {
        return source.getAllPets(page, pageSize);
    }

    public Call<PetResponse> getPetById(String petId) {
        return source.getPetById(petId);
    }

    public Call<PetResponse> updatePet(String petId, UpdatePetRequest body) {
        return source.updatePet(petId, body);
    }

    public Call<Void> deletePet(String petId) {
        return source.deletePet(petId);
    }

    public Call<PetResponse> updatePetPhoto(String petId, Integer photoIndex, UploadFile body) {
        return source.updatePetPhoto(petId, photoIndex, body);
    }
}
