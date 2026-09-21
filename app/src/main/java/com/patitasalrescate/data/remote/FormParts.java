package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.UploadFile;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MultipartBody;

final class FormParts {
    private final List<MultipartBody.Part> parts = new ArrayList<>();

    void field(String name, Object value) {
        if (value != null) parts.add(MultipartBody.Part.createFormData(name, value.toString()));
    }

    void file(String name, UploadFile value) {
        if (value != null) parts.add(MultipartBody.Part.createFormData(name, value.filename, value.content));
    }

    void files(String name, List<UploadFile> values) {
        if (values != null) for (UploadFile value : values) file(name, value);
    }

    List<MultipartBody.Part> build() {
        if (parts.isEmpty()) throw new IllegalArgumentException("Provide at least one form field");
        return parts;
    }
}
