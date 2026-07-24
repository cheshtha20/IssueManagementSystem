package com.issuemanage.auth.controller;

import com.issuemanage.auth.dto.UserResponse;
import com.issuemanage.auth.service.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody String role) {
        // Remove quotes if present from raw string body
        String roleStr = role.replace("\"", "").trim();
        com.issuemanage.auth.model.Role newRole = com.issuemanage.auth.model.Role.valueOf(roleStr);
        return ResponseEntity.ok(userService.updateUserRole(id, newRole));
    }

    @GetMapping("/assignees")
    public ResponseEntity<List<UserResponse>> getActiveAssignees() {
        return ResponseEntity.ok(userService.getAllEnabledAssignees());
    }
}
