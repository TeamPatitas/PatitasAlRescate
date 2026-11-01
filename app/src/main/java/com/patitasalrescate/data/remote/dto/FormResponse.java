package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class FormResponse {
    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("fileUrl")
    public String fileUrl;
    @SerializedName("createdAt")
    public String createdAt;
    @SerializedName("shelterId")
    public String shelterId;
}
