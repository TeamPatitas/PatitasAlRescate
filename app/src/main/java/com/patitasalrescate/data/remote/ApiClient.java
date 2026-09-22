package com.patitasalrescate.data.remote;

import com.google.gson.GsonBuilder;
import com.patitasalrescate.data.repository.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Share one instance so all repositories use the same HTTP pool and JWT session. */
public final class ApiClient {
    public static final String BASE_URL = "https://api-patitasalrescate.galaxym4.dev/";
    public final ApiSession session;
    public final AdminApiRepository admin;
    public final AuthApiRepository auth;
    public final PetApiRepository pets;
    public final ShelterApiRepository shelters;
    public final StatusApiRepository status;

    public ApiClient() { this(BASE_URL, new ApiSession(), new OkHttpClient()); }

    /** Custom URL/client for local contract tests. Production uses BASE_URL. */
    public ApiClient(String baseUrl, ApiSession session, OkHttpClient client) {
        this.session = Objects.requireNonNull(session);
        OkHttpClient http = client.newBuilder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .followRedirects(false)
                .followSslRedirects(false)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    String path = request.url().encodedPath();
                    boolean publicRoute = path.equals("/") || path.equals("/auth/login")
                            || path.equals("/auth/register") || path.equals("/auth/verify-email");
                    String token = session.token();
                    Request.Builder builder = request.newBuilder();
                    if (!publicRoute && token != null) builder.header("Authorization", "Bearer " + token);
                    return chain.proceed(builder.build());
                }).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(http)
                .addConverterFactory(new Converter.Factory() {
                    @Override public Converter<ResponseBody, ?> responseBodyConverter(
                            Type type, Annotation[] annotations, Retrofit retrofit) {
                        if (type != String.class) return null;
                        return (Converter<ResponseBody, String>) body -> {
                            try (ResponseBody response = body) { return response.string(); }
                        };
                    }
                })
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .build();
        admin = new AdminApiRepository(new AdminApiDataSource(retrofit));
        auth = new AuthApiRepository(new AuthApiDataSource(retrofit));
        pets = new PetApiRepository(new PetApiDataSource(retrofit));
        shelters = new ShelterApiRepository(new ShelterApiDataSource(retrofit));
        status = new StatusApiRepository(new StatusApiDataSource(retrofit));
    }
}
