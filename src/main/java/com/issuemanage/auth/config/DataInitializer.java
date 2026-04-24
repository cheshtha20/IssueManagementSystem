package com.issuemanage.auth.config;

import com.issuemanage.auth.model.Role;
import com.issuemanage.auth.model.User;
import com.issuemanage.auth.repository.UserRepository;
import com.issuemanage.ticket.model.SupportTeam;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUser("admin", "admin@ims.com", "Admin@123", Role.ROLE_ADMIN, null);
        seedUser("raiser", "raiser@ims.com", "Raiser@123", Role.ROLE_EMPLOYEE, null);
        seedUser("assignee", "assignee@ims.com", "Assignee@123", Role.ROLE_SUPPORT_ENGINEER, SupportTeam.DEVELOPMENT);
        seedUser("opsresolver", "opsresolver@ims.com", "Ops@12345", Role.ROLE_SUPPORT_ENGINEER, SupportTeam.OPERATIONS);
        seedUser("netresolver", "netresolver@ims.com", "Net@12345", Role.ROLE_SUPPORT_ENGINEER, SupportTeam.NETWORK);
        seedUser("teamlead", "teamlead@ims.com", "TeamLead@123", Role.ROLE_TEAM_LEAD, SupportTeam.DEVELOPMENT);
    }

    private void seedUser(String username, String email, String rawPassword, Role role, SupportTeam team) {
        userRepository.findByUsername(username)
                .ifPresentOrElse(existingUser -> {
                    existingUser.setEmail(email);
                    existingUser.setRole(role);
                    existingUser.setTeam(team);
                    existingUser.setPassword(passwordEncoder.encode(rawPassword));
                    existingUser.setEnabled(true);
                    existingUser.setEmailVerified(true);
                    userRepository.save(existingUser);
                }, () -> {
                    if (!userRepository.existsByEmail(email)) {
                        userRepository.save(new User(
                                username,
                                email,
                                passwordEncoder.encode(rawPassword),
                                role,
                                team,
                                true,
                                true
                        ));
                    }
                });
    }
}
