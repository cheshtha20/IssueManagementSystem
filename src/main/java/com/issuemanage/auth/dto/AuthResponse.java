package com.issuemanage.auth.dto;

public class AuthResponse {

    private final String token;
    private final String tokenType;
    private final String username;
    private final String email;
    private final String role;

    public AuthResponse(String token, String username, String email, String role) {
        this.token = token;
        this.tokenType = "Bearer";
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
