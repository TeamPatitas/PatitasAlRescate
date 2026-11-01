package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import retrofit2.http.*;

interface ShelterApiService {
    @Multipart
    @POST("shelter")
    Call<ShelterResponse> createShelter(@Part java.util.List<okhttp3.MultipartBody.Part> parts);

    @GET("shelter")
    Call<ShelterSummaryResponsePagedResponse> getAllShelters(@Query("page") Integer page, @Query("pageSize") Integer pageSize);

    @Multipart
    @PATCH("shelter/{id}")
    Call<ShelterResponse> updateShelter(@Path("id") String id, @Part java.util.List<okhttp3.MultipartBody.Part> parts);

    @GET("shelter/{id}")
    Call<ShelterResponse> getShelterById(@Path("id") String id);

    @DELETE("shelter/{id}")
    Call<Void> deleteShelter(@Path("id") String id);

    @Multipart
    @POST("shelter/form")
    Call<FormResponse> uploadShelterForm(@Part java.util.List<okhttp3.MultipartBody.Part> parts);

    @GET("shelter/forms")
    Call<FormResponsePagedResponse> getShelterForms();

    @GET("shelter/forms")
    Call<FormResponsePagedResponse> getShelterForms(@Query("page") Integer page, @Query("pageSize") Integer pageSize);

    @GET("shelter/form/{id}")
    Call<FormResponse> getShelterFormById(@Path("id") String id);
}
