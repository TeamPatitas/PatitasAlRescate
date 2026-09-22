package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class UserResponse {
    @SerializedName("id")
    public String id;
    @SerializedName("firstName")
    public String firstName;
    @SerializedName("lastName")
    public String lastName;
    @SerializedName("email")
    public String email;
    @SerializedName("isEmailConfirmed")
    public Boolean isEmailConfirmed;
    @SerializedName("gender")
    public Integer gender;
    @SerializedName("photoUrl")
    public String photoUrl;
    @SerializedName("birthDate")
    public String birthDate;
    @SerializedName("roles")
    public java.util.List<String> roles;
    @SerializedName("shelterId")
    public String shelterId;
}
