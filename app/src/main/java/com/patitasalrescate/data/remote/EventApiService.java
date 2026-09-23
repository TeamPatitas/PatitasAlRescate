package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import retrofit2.http.*;

interface EventApiService {
    @Multipart
    @POST("event")
    Call<EventResponse> createEvent(@Part java.util.List<okhttp3.MultipartBody.Part> parts);

    @GET("event")
    Call<EventSummaryResponsePagedResponse> getAllEvents(@Query("page") Integer page, @Query("pageSize") Integer pageSize);

    @GET("event/{eventId}")
    Call<EventResponse> getEventById(@Path("eventId") String eventId);

    @Multipart
    @PATCH("event/{eventId}")
    Call<EventResponse> updateEvent(@Path("eventId") String eventId, @Part java.util.List<okhttp3.MultipartBody.Part> parts);

    @DELETE("event/{eventId}")
    Call<String> deleteEvent(@Path("eventId") String eventId);
}
