package com.issuemanage.auth.controller;

import com.issuemanage.auth.dto.AuthResponse;
import com.issuemanage.auth.dto.EmailRequest;
import com.issuemanage.auth.dto.LoginRequest;
import com.issuemanage.auth.dto.MessageResponse;
import com.issuemanage.auth.dto.ResetPasswordRequest;
import com.issuemanage.auth.dto.VerifyOtpRequest;
import com.issuemanage.auth.dto.UserResponse;
import com.issuemanage.auth.dto.CreateUserRequest;
import com.issuemanage.auth.service.AuthService;
import com.issuemanage.auth.service.UserService;
import com.issuemanage.auth.service.otp.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final OtpService otpService;
    private final com.issuemanage.auth.service.CaptchaService captchaService;

    public AuthController(AuthService authService, UserService userService, OtpService otpService, com.issuemanage.auth.service.CaptchaService captchaService) {
        this.authService = authService;
        this.userService = userService;
        this.otpService = otpService;
        this.captchaService = captchaService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        if (!captchaService.isValid(request.getCaptchaToken())) {
            throw new RuntimeException("Invalid reCAPTCHA. Please verify you are human.");
        }
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PostMapping("/email-verification/request")
    public ResponseEntity<MessageResponse> requestVerificationOtp(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(otpService.resendEmailVerificationOtp(request));
    }

    @PostMapping("/email-verification/confirm")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(otpService.verifyEmailOtp(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(otpService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(otpService.resetPassword(request));
    }
}
