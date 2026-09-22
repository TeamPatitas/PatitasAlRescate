package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class SwitchRolesRequest {
    @SerializedName("userId")
    public String userId;
    @SerializedName("roles")
    public java.util.List<String> roles;
}
