package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import com.patitasalrescate.data.source.IStatusApiDataSource;

public final class StatusApiDataSource implements IStatusApiDataSource {
    private final StatusApiService service;

    public StatusApiDataSource(retrofit2.Retrofit retrofit) {
        service = retrofit.create(StatusApiService.class);
    }

    @Override public Call<String> getStatus() {
        return service.getStatus();
    }
}
