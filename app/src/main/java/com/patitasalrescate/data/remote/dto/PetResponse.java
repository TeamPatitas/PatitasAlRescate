package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class PetResponse {
    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("specie")
    public Species specie;
    @SerializedName("breed")
    public String breed;
    @SerializedName("gender")
    public Gender gender;
    @SerializedName("temperament")
    public String temperament;
    @SerializedName("story")
    public String story;
    @SerializedName("photos")
    public java.util.List<String> photos;
    @SerializedName("available")
    public Boolean available;
    @SerializedName("shelterId")
    public String shelterId;
}
