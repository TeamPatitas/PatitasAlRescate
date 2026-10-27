package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.*;
import retrofit2.Call;
import com.patitasalrescate.data.source.IEventApiDataSource;

public final class EventApiDataSource implements IEventApiDataSource {
    private final EventApiService service;

    public EventApiDataSource(retrofit2.Retrofit retrofit) {
        service = retrofit.create(EventApiService.class);
    }

    @Override public Call<EventResponse> createEvent(CreateEventRequest body) {
        java.util.Objects.requireNonNull(body, "body");
        FormParts parts = new FormParts();
        parts.field("name", body.name);
        parts.field("eventDate", body.eventDate);
        parts.field("createdAt", body.createdAt);
        parts.field("description", body.description);
        parts.field("latitude", body.latitude);
        parts.field("longitude", body.longitude);
        parts.field("isActive", body.isActive);
        parts.file("image", body.image);
        return service.createEvent(parts.build());
    }

    @Override public Call<EventSummaryResponsePagedResponse> getAllEvents(Integer page, Integer pageSize) {
        if (page != null && page < 1) throw new IllegalArgumentException("page must be positive");
        if (pageSize != null && pageSize < 1) throw new IllegalArgumentException("pageSize must be positive");
        return service.getAllEvents(page, pageSize);
    }

    @Override public Call<EventResponse> getEventById(String eventId) {
        java.util.Objects.requireNonNull(eventId, "eventId");
        return service.getEventById(eventId);
    }

    @Override public Call<EventResponse> updateEvent(String eventId, UpdateEventRequest body) {
        java.util.Objects.requireNonNull(eventId, "eventId");
        java.util.Objects.requireNonNull(body, "body");
        FormParts parts = new FormParts();
        parts.field("name", body.name);
        parts.field("eventDate", body.eventDate);
        parts.field("createdAt", body.createdAt);
        parts.field("description", body.description);
        parts.field("latitude", body.latitude);
        parts.field("longitude", body.longitude);
        parts.field("isActive", body.isActive);
        parts.file("image", body.image);
        return service.updateEvent(eventId, parts.build());
    }

    @Override public Call<String> deleteEvent(String eventId) {
        java.util.Objects.requireNonNull(eventId, "eventId");
        return service.deleteEvent(eventId);
    }
}
