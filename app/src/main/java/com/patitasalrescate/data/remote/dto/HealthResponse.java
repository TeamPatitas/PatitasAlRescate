package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class HealthResponse {
    @SerializedName("status")
    public String status;
    @SerializedName("services")
    public java.util.Map<String, HealthServiceResult> services;
}
