package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class UpdateShelterRequest {
    @SerializedName("name")
    public String name;
    @SerializedName("address")
    public String address;
    @SerializedName("phoneNumber")
    public String phoneNumber;
    @SerializedName("latitude")
    public Double latitude;
    @SerializedName("longitude")
    public Double longitude;
    @SerializedName("photo")
    public UploadFile photo;
    @SerializedName("yapeQrImage")
    public UploadFile yapeQrImage;
}
