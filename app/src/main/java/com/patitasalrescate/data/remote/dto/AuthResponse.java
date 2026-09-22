package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class AuthResponse {
    @SerializedName("token")
    public String token;
    @SerializedName("roles")
    public java.util.List<String> roles;
}
