package com.patitasalrescate.data.remote;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.patitasalrescate.data.remote.dto.*;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static org.junit.Assert.*;

/** Exercises the complete public repository boundary over real local HTTP. */
public class ApiClientTest {
    private static final String ID = "11111111-1111-1111-1111-111111111111";
    private MockWebServer server;
    private ApiClient api;

    @Before public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        api = new ApiClient(server.url("/").toString(), new ApiSession(), new OkHttpClient());
        api.session.setToken("test-jwt");
    }

    @After public void tearDown() throws Exception { server.shutdown(); }

    private RecordedRequest check(Call<?> call, String method, String path, int status, String body)
            throws Exception {
        server.enqueue(new MockResponse().setResponseCode(status).setBody(body));
        Response<?> response = call.execute();
        assertTrue(response.isSuccessful());
        RecordedRequest request = server.takeRequest(3, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals(method, request.getMethod());
        assertEquals(path, request.getPath());
        boolean publicRoute = path.equals("/") || path.startsWith("/auth/login")
                || path.startsWith("/auth/register") || path.startsWith("/auth/verify-email");
        assertEquals(publicRoute ? null : "Bearer test-jwt", request.getHeader("Authorization"));
        return request;
    }

    private UploadFile photo(String name) {
        return new UploadFile(name, RequestBody.create(MediaType.get("image/png"), new byte[]{1, 2, 3}));
    }

    @Test public void allNineAdminOperations() throws Exception {
        check(api.admin.healthCheck(), "GET", "/admin/health", 200, "{\"status\":\"Healthy\",\"services\":{}}");
        check(api.admin.getCurrentUser(), "GET", "/admin/user", 200, "{}");
        UpdateUserRequest update = new UpdateUserRequest();
        update.firstName = "José";
        update.gender = 0;
        update.birthDate = "2001-02-03";
        update.photo = photo("perfil.png");
        String form = check(api.admin.updateCurrentUser(update), "PATCH", "/admin/user", 200, "{}")
                .getBody().readUtf8();
        assertTrue(form.contains("name=\"firstName\""));
        assertTrue(form.contains("José"));
        assertTrue(form.contains("2001-02-03"));
        assertTrue(form.contains("filename=\"perfil.png\""));
        assertFalse(form.contains("lastName"));
        check(api.admin.deleteUser(ID), "DELETE", "/admin/user/" + ID, 200, "true");
        SwitchRolesRequest roles = new SwitchRolesRequest();
        roles.userId = ID;
        roles.roles = Arrays.asList("User", "ShelterOwner");
        RecordedRequest add = check(api.admin.addRoles(roles), "PATCH", "/admin/add-roles", 200, "");
        assertTrue(add.getHeader("Content-Type").startsWith("application/json"));
        JsonObject json = JsonParser.parseString(add.getBody().readUtf8()).getAsJsonObject();
        assertEquals(ID, json.get("userId").getAsString());
        assertEquals(2, json.getAsJsonArray("roles").size());
        check(api.admin.removeRoles(roles), "PATCH", "/admin/remove-roles", 200, "");
        check(api.admin.getAllUsers(2, 10), "GET", "/admin/users?page=2&pageSize=10", 200, "{\"items\":[]}");
        check(api.admin.enableShelter(ID), "PATCH", "/admin/shelter/enable/" + ID, 200, "{}");
        check(api.admin.disableShelter(ID), "PATCH", "/admin/shelter/disable/" + ID, 200, "{}");
    }

    @Test public void allFourAuthOperations() throws Exception {
        LoginRequest login = new LoginRequest();
        login.email = "test@example.invalid";
        login.password = "raw-password";
        RecordedRequest request = check(api.auth.login(login), "POST", "/auth/login", 200,
                "{\"token\":\"jwt\",\"roles\":[\"User\"]}");
        JsonObject json = JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
        assertEquals("raw-password", json.get("password").getAsString());
        RegisterRequest register = new RegisterRequest();
        register.firstName = "José";
        register.lastName = "Pérez";
        register.email = login.email;
        register.password = login.password;
        register.birthDate = "2000-02-01";
        register.gender = 0;
        register.photo = photo("perfil.png");
        String form = check(api.auth.register(register), "POST", "/auth/register", 200,
                "{\"token\":\"jwt\",\"roles\":[]}").getBody().readUtf8();
        assertTrue(form.contains("name=\"birthDate\""));
        assertTrue(form.contains("2000-02-01"));
        assertTrue(form.contains("Content-Type: image/png"));
        check(api.auth.verifyEmail(ID, "a+b/c==&x"), "GET",
                "/auth/verify-email?userId=" + ID + "&token=a%2Bb%2Fc%3D%3D%26x", 200, "");
        check(api.auth.sendVerificationEmail(), "GET", "/auth/send-verification-email", 200, "");
    }

    @Test public void allSixPetOperationsAndMultipartPhotos() throws Exception {
        CreatePetRequest create = new CreatePetRequest();
        create.name = "Luna";
        create.species = Species.CAT;
        create.gender = Gender.FEMALE;
        create.breed = "Mestiza";
        create.temperament = "Tranquila";
        create.story = "Rescatada";
        create.available = false;
        create.photos = Arrays.asList(photo("uno.png"), photo("dos.png"));
        RecordedRequest request = check(api.pets.createPet(create), "POST", "/pet", 201, "{}");
        assertTrue(request.getHeader("Content-Type").startsWith("multipart/form-data; boundary="));
        String form = request.getBody().readUtf8();
        assertEquals(2, form.split("name=\"photos\"", -1).length - 1);
        assertTrue(form.contains("CAT"));
        assertTrue(form.contains("false"));
        check(api.pets.getAllPets(null, null), "GET", "/pet", 200, "{\"items\":[]}");
        check(api.pets.getPetById(ID), "GET", "/pet/" + ID, 200, "{}");
        UpdatePetRequest update = new UpdatePetRequest();
        update.species = Species.DOG;
        update.available = false;
        JsonObject json = JsonParser.parseString(check(api.pets.updatePet(ID, update), "PATCH",
                "/pet/" + ID, 200, "{}").getBody().readUtf8()).getAsJsonObject();
        assertEquals("DOG", json.get("species").getAsString());
        assertFalse(json.get("available").getAsBoolean());
        assertFalse(json.has("name"));
        check(api.pets.deletePet(ID), "DELETE", "/pet/" + ID, 204, "");
        String photo = check(api.pets.updatePetPhoto(ID, 1, photo("nueva.png")), "PATCH",
                "/pet/" + ID + "/photo/1", 200, "{}").getBody().readUtf8();
        assertTrue(photo.contains("name=\"photo\"; filename=\"nueva.png\""));
    }

    @Test public void allFiveShelterOperationsAndStatus() throws Exception {
        CreateShelterRequest create = new CreateShelterRequest();
        create.name = "Refugio";
        create.address = "Lima";
        create.latitude = -12.0;
        create.longitude = -77.0;
        create.photo = photo("refugio.png");
        String form = check(api.shelters.createShelter(create), "POST", "/shelter", 201, "{}")
                .getBody().readUtf8();
        assertTrue(form.contains("-12.0"));
        check(api.shelters.getAllShelters(1, 20), "GET", "/shelter?page=1&pageSize=20", 200, "{\"items\":[]}");
        check(api.shelters.getShelterById(ID), "GET", "/shelter/" + ID, 200, "{}");
        UpdateShelterRequest update = new UpdateShelterRequest();
        update.name = "Nuevo nombre";
        check(api.shelters.updateShelter(ID, update), "PATCH", "/shelter/" + ID, 200, "{}");
        check(api.shelters.deleteShelter(ID), "DELETE", "/shelter/" + ID, 200, "");
        server.enqueue(new MockResponse().setBody("Hola pez"));
        assertEquals("Hola pez", api.status.getStatus().execute().body());
        assertNull(server.takeRequest().getHeader("Authorization"));
    }

    @Test public void allFiveEventOperations() throws Exception {
        CreateEventRequest create = new CreateEventRequest();
        create.name = "Feria";
        create.eventDate = "2026-10-01T10:00:00-05:00";
        create.isActive = true;
        create.image = photo("feria.png");
        String body = check(api.events.createEvent(create), "POST", "/event", 201, "{}")
                .getBody().readUtf8();
        assertTrue(body.contains("name=\"eventDate\""));
        assertTrue(body.contains("filename=\"feria.png\""));
        check(api.events.getAllEvents(1, 20), "GET", "/event?page=1&pageSize=20", 200,
                "{\"items\":[],\"totalPages\":0}");
        check(api.events.getEventById(ID), "GET", "/event/" + ID, 200,
                "{\"id\":\"" + ID + "\",\"name\":\"Feria\",\"isYours\":true}");
        UpdateEventRequest update = new UpdateEventRequest();
        update.name = "Feria nueva";
        update.isActive = false;
        String patch = check(api.events.updateEvent(ID, update), "PATCH", "/event/" + ID, 200, "{}")
                .getBody().readUtf8();
        assertTrue(patch.contains("false"));
        assertFalse(patch.contains("name=\"description\""));
        check(api.events.deleteEvent(ID), "DELETE", "/event/" + ID, 200, "\"Eliminado\"");
    }

    @Test public void deserializeWireNamesPaginationAndSessionLifecycle() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"token\":\"new-jwt\",\"roles\":[\"User\"]}"));
        LoginRequest login = new LoginRequest();
        login.email = "test@example.invalid";
        login.password = "password";
        AuthResponse auth = api.auth.login(login).execute().body();
        api.session.authenticate(auth);
        assertEquals("User", auth.roles.get(0));
        server.takeRequest();
        server.enqueue(new MockResponse().setBody("{\"specie\":\"CAT\",\"gender\":\"FEMALE\",\"available\":false}"));
        PetResponse pet = api.pets.getPetById(ID).execute().body();
        assertEquals(Species.CAT, pet.specie);
        assertEquals(Gender.FEMALE, pet.gender);
        assertEquals(Boolean.FALSE, pet.available);
        assertEquals("Bearer new-jwt", server.takeRequest().getHeader("Authorization"));
        server.enqueue(new MockResponse().setBody("{\"page\":2,\"pageSize\":1,\"totalCount\":3,\"totalPages\":3,\"items\":[{\"id\":\"x\",\"name\":\"Luna\",\"available\":true}]}"));
        PetSummaryResponsePagedResponse page = api.pets.getAllPets(2, 1).execute().body();
        assertEquals(Integer.valueOf(3), page.totalCount);
        assertEquals("Luna", page.items.get(0).name);
        server.takeRequest();
        api.session.logout();
        server.enqueue(new MockResponse().setResponseCode(401));
        assertEquals(401, api.admin.getCurrentUser().execute().code());
        assertNull(server.takeRequest().getHeader("Authorization"));
    }

    @Test public void httpErrorsPreserveStatusBodyAndCooldownWithoutRetry() throws Exception {
        for (int code : new int[]{400, 401, 403, 404, 409, 429, 500}) {
            server.enqueue(new MockResponse().setResponseCode(code).addHeader("Retry-After", "300")
                    .setBody("{\"detail\":\"failed\"}"));
            ApiError error = ApiError.from(api.admin.enableShelter(ID).execute());
            assertEquals(code, error.statusCode);
            assertTrue(error.body.contains("failed"));
            assertEquals("300", error.retryAfter);
            server.takeRequest();
        }
        assertEquals(7, server.getRequestCount());
    }

    @Test public void malformedJsonIsDeliveredAsAsyncFailure() throws Exception {
        server.enqueue(new MockResponse().setBody("not-json"));
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        api.admin.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override public void onResponse(Call<UserResponse> call, Response<UserResponse> response) { done.countDown(); }
            @Override public void onFailure(Call<UserResponse> call, Throwable error) { failure.set(error); done.countDown(); }
        });
        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertNotNull(failure.get());
    }

    @Test public void rejectsInvalidInputsBeforeNetwork() {
        assertThrows(IllegalArgumentException.class, () -> api.pets.getAllPets(0, 10));
        assertThrows(IllegalArgumentException.class, () -> api.shelters.getAllShelters(1, -1));
        assertThrows(IllegalArgumentException.class, () -> api.admin.updateCurrentUser(new UpdateUserRequest()));
        assertThrows(IllegalArgumentException.class, () -> api.session.setToken("bad\r\ntoken"));
        assertThrows(NullPointerException.class, () -> api.pets.getPetById(null));
        assertEquals(0, server.getRequestCount());
    }

    @Test public void cancellationAndOfflineFailuresAreNotHttpSuccess() throws Exception {
        Call<UserResponse> cancelled = api.admin.getCurrentUser();
        cancelled.cancel();
        assertTrue(cancelled.isCanceled());
        assertThrows(java.io.IOException.class, cancelled::execute);
        try (java.net.ServerSocket unusedPort = new java.net.ServerSocket(0)) {
            int port = unusedPort.getLocalPort();
            unusedPort.close();
            ApiClient offline = new ApiClient("http://127.0.0.1:" + port + "/",
                    new ApiSession(), new OkHttpClient());
            assertThrows(java.io.IOException.class, () -> offline.admin.getCurrentUser().execute());
        }
    }

    @Test public void redirectsDoNotForwardCredentialsOrRepeatWrites() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(307).addHeader("Location", server.url("/other")));
        Response<?> response = api.admin.enableShelter(ID).execute();
        assertEquals(307, response.code());
        assertEquals(1, server.getRequestCount());
        assertEquals("/admin/shelter/enable/" + ID, server.takeRequest().getPath());
    }
}
