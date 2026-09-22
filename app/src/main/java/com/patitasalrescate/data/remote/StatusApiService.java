package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import retrofit2.http.*;

interface StatusApiService {
    @GET("./")
    Call<String> getStatus();
}
