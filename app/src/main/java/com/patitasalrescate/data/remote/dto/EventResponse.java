package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class EventResponse {
    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("eventDate")
    public String eventDate;
    @SerializedName("createdAt")
    public String createdAt;
    @SerializedName("description")
    public String description;
    @SerializedName("latitude")
    public String latitude;
    @SerializedName("longitude")
    public String longitude;
    @SerializedName("photoUrl")
    public String photoUrl;
    @SerializedName("isActive")
    public Boolean isActive;
    @SerializedName("shelterId")
    public String shelterId;
    @SerializedName("isYours")
    public Boolean isYours;
}
