package com.patitasalrescate.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Wire model from docs/api/openapi.json. Dates use ISO yyyy-MM-dd. */
public final class UserSummaryResponsePagedResponse {
    @SerializedName("items")
    public java.util.List<UserSummaryResponse> items;
    @SerializedName("page")
    public Integer page;
    @SerializedName("pageSize")
    public Integer pageSize;
    @SerializedName("totalCount")
    public Integer totalCount;
    @SerializedName("totalPages")
    public Integer totalPages;
}
