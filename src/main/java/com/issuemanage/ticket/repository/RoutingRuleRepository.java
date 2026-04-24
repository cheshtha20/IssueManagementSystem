package com.issuemanage.ticket.repository;

import com.issuemanage.ticket.model.RoutingRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutingRuleRepository extends JpaRepository<RoutingRule, Long> {
    List<RoutingRule> findByActiveTrueOrderByConfidenceScoreDescIdAsc();
}
