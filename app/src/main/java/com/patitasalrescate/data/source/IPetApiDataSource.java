package com.patitasalrescate.data.source;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;

/** Remote operations; enqueue calls off the UI thread. */
public interface IPetApiDataSource {
    Call<PetResponse> createPet(CreatePetRequest body);
    Call<PetSummaryResponsePagedResponse> getAllPets(Integer page, Integer pageSize);
    Call<PetResponse> getPetById(String petId);
    Call<PetResponse> updatePet(String petId, UpdatePetRequest body);
    Call<Void> deletePet(String petId);
    Call<PetResponse> updatePetPhoto(String petId, Integer photoIndex, UploadFile body);
}
