package com.patitasalrescate.data.source;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;

/** Remote operations; enqueue calls off the UI thread. */
public interface IEventApiDataSource {
    Call<EventResponse> createEvent(CreateEventRequest body);
    Call<EventSummaryResponsePagedResponse> getAllEvents(Integer page, Integer pageSize);
    Call<EventResponse> getEventById(String eventId);
    Call<EventResponse> updateEvent(String eventId, UpdateEventRequest body);
    Call<String> deleteEvent(String eventId);
}
