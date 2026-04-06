package com.assignment.backend.dto;

import com.assignment.backend.model.Role;

public class AuthResponse {

    private final String token;
    private final String type;
    private final String username;
    private final Role role;

    public AuthResponse(String token, String type, String username, Role role) {
        this.token = token;
        this.type = type;
        this.username = username;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }
}
