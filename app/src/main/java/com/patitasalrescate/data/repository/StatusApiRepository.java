package com.patitasalrescate.data.repository;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import com.patitasalrescate.data.source.IStatusApiDataSource;

/** API repository, injectable independently of the existing local repositories. */
public final class StatusApiRepository {
    private final IStatusApiDataSource source;

    public StatusApiRepository(IStatusApiDataSource source) {
        this.source = java.util.Objects.requireNonNull(source);
    }

    public Call<String> getStatus() {
        return source.getStatus();
    }
}
