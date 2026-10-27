package com.patitasalrescate.data.remote;

import com.patitasalrescate.data.remote.dto.AuthResponse;

/** In-memory JWT session. No credentials are written to disk or logs. */
public final class ApiSession {
    private volatile String token;

    public void authenticate(AuthResponse response) {
        if (response == null || response.token == null || response.token.trim().isEmpty()) {
            throw new IllegalArgumentException("Authentication response has no token");
        }
        setToken(response.token);
    }

    public void setToken(String token) {
        if (token == null || token.trim().isEmpty() || token.contains("\n") || token.contains("\r")) {
            throw new IllegalArgumentException("Invalid token");
        }
        this.token = token;
    }

    String token() { return token; }

    public boolean isAuthenticated() { return token != null; }

    public void logout() { token = null; }
}
