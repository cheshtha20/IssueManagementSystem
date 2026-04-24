package com.issuemanage.ticket.config;

import com.issuemanage.ticket.model.IssueCategory;
import com.issuemanage.ticket.model.RoutingRule;
import com.issuemanage.ticket.model.SupportTeam;
import com.issuemanage.ticket.repository.RoutingRuleRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoutingRuleDataInitializer implements CommandLineRunner {

    private final RoutingRuleRepository routingRuleRepository;

    public RoutingRuleDataInitializer(RoutingRuleRepository routingRuleRepository) {
        this.routingRuleRepository = routingRuleRepository;
    }

    @Override
    public void run(String... args) {
        if (routingRuleRepository.count() == 0) {
            routingRuleRepository.saveAll(List.of(
                    new RoutingRule(IssueCategory.BUG, "login", SupportTeam.DEVELOPMENT, 90, true),
                    new RoutingRule(IssueCategory.BUG, "error", SupportTeam.DEVELOPMENT, 80, true),
                    new RoutingRule(IssueCategory.BUG, "database", SupportTeam.DEVELOPMENT, 85, true),
                    new RoutingRule(IssueCategory.ENHANCEMENT, "feature", SupportTeam.DEVELOPMENT, 88, true),
                    new RoutingRule(IssueCategory.ENHANCEMENT, "improve", SupportTeam.DEVELOPMENT, 78, true),
                    new RoutingRule(IssueCategory.OPERATIONS, "access", SupportTeam.OPERATIONS, 90, true),
                    new RoutingRule(IssueCategory.OPERATIONS, "deploy", SupportTeam.OPERATIONS, 84, true),
                    new RoutingRule(IssueCategory.OPERATIONS, "server", SupportTeam.OPERATIONS, 75, true),
                    new RoutingRule(IssueCategory.NETWORKING, "vpn", SupportTeam.NETWORK, 92, true),
                    new RoutingRule(IssueCategory.NETWORKING, "connect", SupportTeam.NETWORK, 86, true),
                    new RoutingRule(IssueCategory.NETWORKING, "latency", SupportTeam.NETWORK, 80, true)
            ));
        }
    }
}
