package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. */
public final class Location {
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

    public Location() {}

    public Location(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
