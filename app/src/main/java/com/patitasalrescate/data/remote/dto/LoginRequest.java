package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class LoginRequest {
    @SerializedName("email")
    public String email;
    @SerializedName("password")
    public String password;
}
