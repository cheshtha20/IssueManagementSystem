package com.issuemanage.auth.model;

import com.issuemanage.ticket.model.SupportTeam;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @jakarta.persistence.Convert(converter = RoleConverter.class)
    @Column(nullable = false, length = 30)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SupportTeam team;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    public User() {
    }

    public User(String username,
                String email,
                String password,
                Role role,
                SupportTeam team,
                boolean enabled,
                boolean emailVerified) {
        this.username = username;
        this.email = email;
        this.password = password;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public SupportTeam getTeam() {
        return team;
    }

    public void setTeam(SupportTeam team) {
        this.team = team;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
