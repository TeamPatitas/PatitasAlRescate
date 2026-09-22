package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class ShelterSummaryResponse {
    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("isAvailable")
    public Boolean isAvailable;
    @SerializedName("photoUrl")
    public String photoUrl;
}
