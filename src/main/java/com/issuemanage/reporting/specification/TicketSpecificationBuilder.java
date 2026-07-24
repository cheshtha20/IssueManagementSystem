package com.issuemanage.reporting.specification;

import com.issuemanage.auth.model.Role;
import com.issuemanage.auth.model.User;
import com.issuemanage.reporting.dto.TicketSearchCriteria;
import com.issuemanage.ticket.model.Ticket;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TicketSpecificationBuilder {

    public Specification<Ticket> build(TicketSearchCriteria criteria, User actor) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (criteria.getKeyword() != null) {
                String keyword = "%" + criteria.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("ticketId")), keyword),
                        cb.like(cb.lower(root.get("title")), keyword),
                        cb.like(cb.lower(cb.coalesce(root.get("description"), "")), keyword)
                ));
            }

            if (criteria.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), criteria.getCategory()));
            }
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), criteria.getPriority()));
            }
            if (criteria.getDepartment() != null) {
                predicates.add(cb.like(cb.lower(cb.coalesce(root.get("department"), "")),
                        "%" + criteria.getDepartment().toLowerCase() + "%"));
            }
            if (criteria.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getStartDate()));
            }
            if (criteria.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getEndDate()));
            }
            if (criteria.getAssignee() != null) {
                Join<Object, Object> assigneeJoin = root.join("assignedTo", JoinType.LEFT);
                String assignee = "%" + criteria.getAssignee().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(assigneeJoin.get("username")), assignee),
                        cb.like(cb.lower(cb.coalesce(assigneeJoin.get("email"), "")), assignee)
                ));
            }

            predicates.add(buildVisibilityPredicate(root, root.join("raisedBy", JoinType.LEFT), root.join("assignedTo", JoinType.LEFT), actor, cb));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private jakarta.persistence.criteria.Predicate buildVisibilityPredicate(jakarta.persistence.criteria.Root<Ticket> root,
                                                                             Join<Object, Object> raisedByJoin,
                                                                             Join<Object, Object> assignedToJoin,
                                                                             User actor,
                                                                             jakarta.persistence.criteria.CriteriaBuilder cb) {
        if (actor.getRole() == Role.ROLE_EMPLOYEE) {
            return cb.equal(raisedByJoin.get("id"), actor.getId());
        }
        if (actor.getRole() == Role.ROLE_SUPPORT_ENGINEER) {
            // Can see tickets assigned to them OR tickets in their assigned team
            jakarta.persistence.criteria.Predicate assignedToMe = cb.equal(assignedToJoin.get("id"), actor.getId());
            
            if (actor.getTeam() != null) {
                jakarta.persistence.criteria.Predicate myTeam = cb.equal(root.get("assignedTeam"), actor.getTeam());
                jakarta.persistence.criteria.Predicate unassigned = cb.isNull(root.get("assignedTo"));
                return cb.or(assignedToMe, cb.and(myTeam, unassigned));
            }
            return assignedToMe;
        }
        return cb.conjunction(); // Admin and Team Lead see everything
    }
}
