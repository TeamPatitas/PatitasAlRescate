package com.patitasalrescate.data.remote.dto;

import java.io.File;
import java.util.Objects;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/** A binary attachment; pass a cache file copied from a content URI when necessary. */
public final class UploadFile {
    public final String filename;
    public final RequestBody content;

    public UploadFile(String filename, RequestBody content) {
        this.filename = Objects.requireNonNull(filename);
        this.content = Objects.requireNonNull(content);
    }

    public static UploadFile fromFile(File file, String mediaType) {
        if (!file.isFile()) throw new IllegalArgumentException("Attachment must be a readable file");
        return new UploadFile(file.getName(), RequestBody.create(MediaType.get(mediaType), file));
    }
}
