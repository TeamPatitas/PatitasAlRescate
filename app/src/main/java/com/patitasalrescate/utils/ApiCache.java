package com.patitasalrescate.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Caché simple en archivos para respuestas GET de la API.
 * Por clave se guardan dos archivos: .ts (millis de guardado) y .json (body crudo).
 * Las mutaciones (POST/PATCH/DELETE) invalidan todo.
 */
public final class ApiCache {
    private ApiCache() { }

    static File dir(Context ctx) {
        File d = new File(ctx.getCacheDir(), "api");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    /** Clave segura para nombre de archivo a partir de método + URL. */
    public static String key(String method, String url) {
        String base = method.toUpperCase() + "_" + Integer.toHexString(url.hashCode())
                + "_" + url.length();
        return base.replace('-', 'n');
    }

    /** Body si existe y es más fresco que ttlMs; null si no hay o venció. */
    public static String getFresh(Context ctx, String key, long ttlMs) {
        Long ts = readTs(ctx, key);
        if (ts == null) return null;
        if (System.currentTimeMillis() - ts > ttlMs) return null;
        return readBody(ctx, key);
    }

    /** Body sin importar la edad (para modo offline). Null si no hay. */
    public static String getAny(Context ctx, String key) {
        return readBody(ctx, key);
    }

    public static void put(Context ctx, String key, String body) {
        if (body == null) return;
        try {
            File d = dir(ctx);
            writeFile(new File(d, key + ".ts"), String.valueOf(System.currentTimeMillis()));
            writeFile(new File(d, key + ".json"), body);
        } catch (Exception ignored) { }
    }

    public static void invalidateAll(Context ctx) {
        try {
            File d = dir(ctx);
            File[] archivos = d.listFiles();
            if (archivos == null) return;
            for (File f : archivos) {
                if (!f.delete()) {
                    //noinspection ResultOfMethodCallIgnored
                    f.deleteOnExit();
                }
            }
        } catch (Exception ignored) { }
    }

    private static Long readTs(Context ctx, String key) {
        try {
            String texto = readFile(new File(dir(ctx), key + ".ts")).trim();
            return Long.parseLong(texto);
        } catch (Exception e) {
            return null;
        }
    }

    private static String readBody(Context ctx, String key) {
        try {
            File f = new File(dir(ctx), key + ".json");
            if (!f.isFile()) return null;
            return readFile(f);
        } catch (Exception e) {
            return null;
        }
    }

    private static String readFile(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        try {
            byte[] datos = new byte[(int) f.length()];
            int leidos = 0;
            while (leidos < datos.length) {
                int n = in.read(datos, leidos, datos.length - leidos);
                if (n < 0) break;
                leidos += n;
            }
            return new String(datos, 0, leidos, StandardCharsets.UTF_8);
        } finally {
            try {
                in.close();
            } catch (Exception ignored) { }
        }
    }

    private static void writeFile(File f, String contenido) throws Exception {
        FileOutputStream out = new FileOutputStream(f);
        try {
            out.write(contenido.getBytes(StandardCharsets.UTF_8));
        } finally {
            try {
                out.close();
            } catch (Exception ignored) { }
        }
    }
}
