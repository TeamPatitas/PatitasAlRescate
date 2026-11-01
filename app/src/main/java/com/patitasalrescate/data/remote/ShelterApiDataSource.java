package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import com.patitasalrescate.data.source.IShelterApiDataSource;

public final class ShelterApiDataSource implements IShelterApiDataSource {
    private static final String TAG = "ShelterApi";
    private final ShelterApiService service;

    public ShelterApiDataSource(retrofit2.Retrofit retrofit) {
        service = retrofit.create(ShelterApiService.class);
    }

    @Override public Call<ShelterResponse> createShelter(CreateShelterRequest body) {
        java.util.Objects.requireNonNull(body, "body");
        FormParts parts = new FormParts();
        parts.field("name", body.name);
        parts.field("phoneNumber", body.phoneNumber);
        parts.field("latitude", body.latitude);
        parts.field("longitude", body.longitude);
        parts.field("showAddress", body.showAddress);
        parts.file("photo", body.photo);
        parts.file("yapeQrImage", body.yapeQrImage);
        return service.createShelter(parts.build());
    }

    @Override public Call<ShelterSummaryResponsePagedResponse> getAllShelters(Integer page, Integer pageSize) {
        if (page != null && page < 1) throw new IllegalArgumentException("page must be positive");
        if (pageSize != null && pageSize < 1) throw new IllegalArgumentException("pageSize must be positive");
        return service.getAllShelters(page, pageSize);
    }

    @Override public Call<ShelterResponse> updateShelter(String id, UpdateShelterRequest body) {
        java.util.Objects.requireNonNull(id, "id");
        java.util.Objects.requireNonNull(body, "body");
        android.util.Log.d(TAG, "updateShelter id=" + id
                + " name=" + (body.name != null)
                + " phoneNumber=" + (body.phoneNumber != null)
                + " showAddress=" + body.showAddress
                + " photo=" + (body.photo != null ? body.photo.filename : null)
                + " yapeQrImage=" + (body.yapeQrImage != null
                ? body.yapeQrImage.filename + "/" + body.yapeQrImage.content.contentType() : null));
        FormParts parts = new FormParts();
        parts.field("name", body.name);
        parts.field("phoneNumber", body.phoneNumber);
        parts.field("latitude", body.latitude);
        parts.field("longitude", body.longitude);
        parts.field("showAddress", body.showAddress);
        parts.file("photo", body.photo);
        parts.file("yapeQrImage", body.yapeQrImage);
        java.util.List<okhttp3.MultipartBody.Part> built = parts.build();
        for (okhttp3.MultipartBody.Part part : built) {
            android.util.Log.d(TAG, "multipart part: " + part.headers());
        }
        return service.updateShelter(id, built);
    }

    @Override public Call<ShelterResponse> getShelterById(String id) {
        java.util.Objects.requireNonNull(id, "id");
        return service.getShelterById(id);
    }

    @Override public Call<Void> deleteShelter(String id) {
        java.util.Objects.requireNonNull(id, "id");
        return service.deleteShelter(id);
    }

    @Override public Call<FormResponse> uploadShelterForm(UploadFormRequest body) {
        java.util.Objects.requireNonNull(body, "body");
        FormParts parts = new FormParts();
        parts.field("name", body.name);
        parts.file("file", body.file);
        return service.uploadShelterForm(parts.build());
    }

    @Override public Call<FormResponsePagedResponse> getShelterForms() {
        return service.getShelterForms();
    }

    @Override public Call<FormResponsePagedResponse> getShelterForms(Integer page, Integer pageSize) {
        if (page != null && page < 1) throw new IllegalArgumentException("page must be positive");
        if (pageSize != null && pageSize < 1) throw new IllegalArgumentException("pageSize must be positive");
        return service.getShelterForms(page, pageSize);
    }

    @Override public Call<FormResponse> getShelterFormById(String id) {
        java.util.Objects.requireNonNull(id, "id");
        return service.getShelterFormById(id);
    }
}
