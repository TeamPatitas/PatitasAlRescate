package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class RegisterRequest {
    @SerializedName("firstName")
    public String firstName;
    @SerializedName("lastName")
    public String lastName;
    @SerializedName("email")
    public String email;
    @SerializedName("password")
    public String password;
    @SerializedName("birthDate")
    public String birthDate;
    @SerializedName("gender")
    public Integer gender;
    @SerializedName("photo")
    public UploadFile photo;
}
