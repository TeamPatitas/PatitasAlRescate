package com.patitasalrescate.data.remote;

import java.io.IOException;
import retrofit2.Response;

/** HTTP failures are distinct from transport/JSON failures delivered to onFailure. */
public final class ApiError {
    public final int statusCode;
    public final String body;
    /** Original header: may be delay seconds or an HTTP date. Never automatically retry writes. */
    public final String retryAfter;

    private ApiError(int statusCode, String body, String retryAfter) {
        this.statusCode = statusCode;
        this.body = body;
        this.retryAfter = retryAfter;
    }

    public static ApiError from(Response<?> response) throws IOException {
        if (response.isSuccessful()) throw new IllegalArgumentException("Response was successful");
        String body = response.errorBody() == null ? "" : response.errorBody().string();
        return new ApiError(response.code(), body, response.headers().get("Retry-After"));
    }
}
