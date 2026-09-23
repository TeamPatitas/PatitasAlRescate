package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class PetSummaryResponse {
    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("photos")
    public java.util.List<String> photos;
    @SerializedName("available")
    public Boolean available;
}
