package com.issuemanage.ticket.controller;

import com.issuemanage.ticket.dto.CommentResponse;
import com.issuemanage.ticket.dto.CreateCommentRequest;
import com.issuemanage.ticket.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> addComment(@PathVariable String ticketId,
                                                      @Valid @RequestBody CreateCommentRequest request,
                                                      Authentication authentication) {
        CommentResponse response = commentService.addComment(ticketId, authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable String ticketId,
                                                             Authentication authentication) {
        return ResponseEntity.ok(commentService.getComments(ticketId, authentication.getName()));
    }
}
