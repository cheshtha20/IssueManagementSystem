package com.issuemanage.auth.service;

import com.issuemanage.auth.dto.CreateUserRequest;
import com.issuemanage.auth.dto.UserResponse;
import com.issuemanage.auth.model.Role;
import com.issuemanage.auth.model.User;
import com.issuemanage.auth.repository.UserRepository;
import com.issuemanage.auth.service.otp.OtpService;
import com.issuemanage.ticket.model.SupportTeam;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (request.getRole() == Role.ROLE_SUPPORT_ENGINEER && request.getTeam() == null) {
            throw new IllegalArgumentException("Team is required for resolver accounts");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole(),
                request.getTeam(),
                true,
                false
        );

        User savedUser = userRepository.save(user);
        otpService.sendEmailVerificationOtp(savedUser);
        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getTeam() == null ? null : savedUser.getTeam().name(),
                savedUser.isEnabled(),
                savedUser.isEmailVerified()
        );
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("Cannot change the role of an Admin user");
        }
        
        user.setRole(newRole);
        User updated = userRepository.save(user);
        return mapToResponse(updated);
    }

    public List<UserResponse> getAllEnabledAssignees() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_SUPPORT_ENGINEER)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAssigneesByTeam(SupportTeam team) {
        return userRepository.findByRoleAndTeam(Role.ROLE_SUPPORT_ENGINEER, team)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getTeam() == null ? null : user.getTeam().name(),
                user.isEnabled(),
                user.isEmailVerified()
        );
    }
}
