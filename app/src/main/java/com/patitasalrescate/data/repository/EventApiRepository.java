package com.patitasalrescate.data.repository;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import com.patitasalrescate.data.source.IEventApiDataSource;

/** API repository, injectable independently of the existing local repositories. */
public final class EventApiRepository {
    private final IEventApiDataSource source;

    public EventApiRepository(IEventApiDataSource source) {
        this.source = java.util.Objects.requireNonNull(source);
    }

    public Call<EventResponse> createEvent(CreateEventRequest body) {
        return source.createEvent(body);
    }

    public Call<EventSummaryResponsePagedResponse> getAllEvents(Integer page, Integer pageSize) {
        return source.getAllEvents(page, pageSize);
    }

    public Call<EventResponse> getEventById(String eventId) {
        return source.getEventById(eventId);
    }

    public Call<EventResponse> updateEvent(String eventId, UpdateEventRequest body) {
        return source.updateEvent(eventId, body);
    }

    public Call<String> deleteEvent(String eventId) {
        return source.deleteEvent(eventId);
    }
}
