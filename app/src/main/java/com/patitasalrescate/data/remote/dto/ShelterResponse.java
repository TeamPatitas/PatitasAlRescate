package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class ShelterResponse {
    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("phoneNumber")
    public String phoneNumber;
    @SerializedName("isAvailable")
    public Boolean isAvailable;
    @SerializedName("location")
    public Location location;
    @SerializedName("latitude")
    public Double latitude;
    @SerializedName("longitude")
    public Double longitude;
    @SerializedName(value = "showAddress", alternate = {
            "show_address", "ShowAddress", "showaddress",
            "isAddressVisible", "is_address_visible", "IsAddressVisible",
            "addressVisible", "address_visible", "mostrarDireccion", "mostrar_direccion"
    })
    public Boolean showAddress;
    @SerializedName("photoUrl")
    public String photoUrl;
    @SerializedName("yapeQrCode")
    public String yapeQrCode;
    @SerializedName("owners")
    public java.util.List<String> owners;
}
