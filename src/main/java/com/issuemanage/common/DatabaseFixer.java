package com.issuemanage.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseFixer.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        logger.info("Checking for database constraints that need fixing...");
        try {
            // Drop enum check constraints in PostgreSQL
            jdbcTemplate.execute("ALTER TABLE ticket_remarks DROP CONSTRAINT IF EXISTS ticket_remarks_remark_type_check");
            jdbcTemplate.execute("ALTER TABLE audit_log DROP CONSTRAINT IF EXISTS audit_log_action_type_check");
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_team_check");
            
            // Dropped old constraints here
            
            // Migrate old ROLE_MANAGER to ROLE_TEAM_LEAD
            int updatedCount = jdbcTemplate.update("UPDATE users SET role = 'ROLE_TEAM_LEAD' WHERE role = 'ROLE_MANAGER'");
            if (updatedCount > 0) {
                logger.info("Migrated {} users from decommissioned ROLE_MANAGER to ROLE_TEAM_LEAD.", updatedCount);
            }

            // Migrate ROLE_TICKET_RAISER to ROLE_EMPLOYEE
            int raiserCount = jdbcTemplate.update("UPDATE users SET role = 'ROLE_EMPLOYEE' WHERE role = 'ROLE_TICKET_RAISER'");
            if (raiserCount > 0) {
                logger.info("Migrated {} users from ROLE_TICKET_RAISER to ROLE_EMPLOYEE.", raiserCount);
            }

            // Migrate ROLE_ASSIGNEE to ROLE_SUPPORT_ENGINEER
            int assigneeCount = jdbcTemplate.update("UPDATE users SET role = 'ROLE_SUPPORT_ENGINEER' WHERE role = 'ROLE_ASSIGNEE'");
            if (assigneeCount > 0) {
                logger.info("Migrated {} users from ROLE_ASSIGNEE to ROLE_SUPPORT_ENGINEER.", assigneeCount);
            }
            
            logger.info("Successfully checked and updated database state.");
        } catch (Exception e) {
            logger.error("Failed to drop database constraint: {}", e.getMessage());
        }
    }
}
