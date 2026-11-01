package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. */
public final class UploadFormRequest {
    @SerializedName("name")
    public String name;
    @SerializedName("file")
    public UploadFile file;
}
