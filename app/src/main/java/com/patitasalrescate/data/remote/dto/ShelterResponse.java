package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class ShelterResponse {
    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("address")
    public String address;
    @SerializedName("isAvailable")
    public Boolean isAvailable;
    @SerializedName("latitude")
    public Double latitude;
    @SerializedName("longitude")
    public Double longitude;
    @SerializedName("photoUrl")
    public String photoUrl;
    @SerializedName("owners")
    public java.util.List<String> owners;
}
