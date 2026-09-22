package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class UpdatePetRequest {
    @SerializedName("name")
    public String name;
    @SerializedName("species")
    public Species species;
    @SerializedName("breed")
    public String breed;
    @SerializedName("gender")
    public Gender gender;
    @SerializedName("temperament")
    public String temperament;
    @SerializedName("story")
    public String story;
    @SerializedName("available")
    public Boolean available;
}
