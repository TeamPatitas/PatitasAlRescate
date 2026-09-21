package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import com.patitasalrescate.data.source.IPetApiDataSource;

public final class PetApiDataSource implements IPetApiDataSource {
    private final PetApiService service;

    public PetApiDataSource(retrofit2.Retrofit retrofit) {
        service = retrofit.create(PetApiService.class);
    }

    @Override public Call<PetResponse> createPet(CreatePetRequest body) {
        java.util.Objects.requireNonNull(body, "body");
        FormParts parts = new FormParts();
        parts.field("name", body.name);
        parts.field("species", body.species);
        parts.field("breed", body.breed);
        parts.field("gender", body.gender);
        parts.field("temperament", body.temperament);
        parts.field("story", body.story);
        parts.field("available", body.available);
        parts.files("photos", body.photos);
        return service.createPet(parts.build());
    }

    @Override public Call<PetSummaryResponsePagedResponse> getAllPets(Integer page, Integer pageSize) {
        if (page != null && page < 1) throw new IllegalArgumentException("page must be positive");
        if (pageSize != null && pageSize < 1) throw new IllegalArgumentException("pageSize must be positive");
        return service.getAllPets(page, pageSize);
    }

    @Override public Call<PetResponse> getPetById(String petId) {
        java.util.Objects.requireNonNull(petId, "petId");
        return service.getPetById(petId);
    }

    @Override public Call<PetResponse> updatePet(String petId, UpdatePetRequest body) {
        java.util.Objects.requireNonNull(petId, "petId");
        java.util.Objects.requireNonNull(body, "body");
        return service.updatePet(petId, body);
    }

    @Override public Call<Void> deletePet(String petId) {
        java.util.Objects.requireNonNull(petId, "petId");
        return service.deletePet(petId);
    }

    @Override public Call<PetResponse> updatePetPhoto(String petId, Integer photoIndex, UploadFile body) {
        java.util.Objects.requireNonNull(petId, "petId");
        java.util.Objects.requireNonNull(photoIndex, "photoIndex");
        java.util.Objects.requireNonNull(body, "body");
        FormParts parts = new FormParts();
        parts.file("photo", body);
        return service.updatePetPhoto(petId, photoIndex, parts.build());
    }
}
