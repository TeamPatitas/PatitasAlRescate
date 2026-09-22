package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import retrofit2.http.*;

interface PetApiService {
    @Multipart
    @POST("pet")
    Call<PetResponse> createPet(@Part java.util.List<okhttp3.MultipartBody.Part> parts);

    @GET("pet")
    Call<PetSummaryResponsePagedResponse> getAllPets(@Query("page") Integer page, @Query("pageSize") Integer pageSize);

    @GET("pet/{petId}")
    Call<PetResponse> getPetById(@Path("petId") String petId);

    @PATCH("pet/{petId}")
    Call<PetResponse> updatePet(@Path("petId") String petId, @Body UpdatePetRequest body);

    @DELETE("pet/{petId}")
    Call<Void> deletePet(@Path("petId") String petId);

    @Multipart
    @PATCH("pet/{petId}/photo/{photoIndex}")
    Call<PetResponse> updatePetPhoto(@Path("petId") String petId, @Path("photoIndex") Integer photoIndex, @Part java.util.List<okhttp3.MultipartBody.Part> parts);
}
