package com.patitasalrescate.data.remote;

import android.content.Context;

import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.ApiCache;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Caché transparente para GET con TTL de 10 minutos:
 * - GET con caché fresca (< TTL): se sirve sin tocar la red.
 * - GET con caché vencida: va a la red y refresca al éxito.
 * - Sin red o fallo: se sirve la caché aunque esté vencida; si no hay, falla normal.
 * - POST/PATCH/DELETE: pasan directo e invalidan toda la caché al ejecutarse.
 * Sin necesidad de tocar las pantallas: Retrofit/Gson parsean igual.
 */
public final class ApiCacheInterceptor implements Interceptor {
    public static final long TTL_MS = 10 * 60 * 1000;
    private static final long MAX_CACHE_BYTES = 4 * 1024 * 1024;

    @Override public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        if (!"GET".equalsIgnoreCase(req.method())) {
            Response resp = chain.proceed(req);
            Context ctx = ApiApp.appContext();
            if (ctx != null) ApiCache.invalidateAll(ctx);
            return resp;
        }
        Context ctx = ApiApp.appContext();
        if (ctx == null) return chain.proceed(req);
        String key = ApiCache.key(req.method(), req.url().toString());

        String fresco = ApiCache.getFresh(ctx, key, TTL_MS);
        if (fresco != null) return cached(req, fresco);

        try {
            Response resp = chain.proceed(req);
            if (resp.isSuccessful() && resp.body() != null) {
                try {
                    String body = resp.peekBody(MAX_CACHE_BYTES).string();
                    ApiCache.put(ctx, key, body);
                } catch (Exception ignored) { }
            }
            return resp;
        } catch (IOException sinRed) {
            String viejo = ApiCache.getAny(ctx, key);
            if (viejo != null) return cached(req, viejo);
            throw sinRed;
        }
    }

    private static Response cached(Request req, String json) {
        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(json, MediaType.get("application/json; charset=utf-8")))
                .addHeader("X-Patitas-Cache", "1")
                .build();
    }
}
