package com.issuemanage.auth.dto;

public class UserResponse {

    private final Long id;
    private final String username;
    private final String email;
    private final String role;
    private final String team;
    private final boolean enabled;
    private final boolean emailVerified;

    public UserResponse(Long id,
                        String username,
                        String email,
                        String role,
                        String team,
                        boolean enabled,
                        boolean emailVerified) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.team = team;
        this.enabled = enabled;
        this.emailVerified = emailVerified;
    }

    public Long getId() {
        return id;
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

    public String getTeam() {
        return team;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
