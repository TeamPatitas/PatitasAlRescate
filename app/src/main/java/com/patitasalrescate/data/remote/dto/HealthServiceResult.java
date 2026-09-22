package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class HealthServiceResult {
    @SerializedName("status")
    public String status;
    @SerializedName("latencyMs")
    public Double latencyMs;
    @SerializedName("error")
    public String error;
}
