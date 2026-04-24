package com.issuemanage.auth.service.otp;

import com.issuemanage.auth.dto.EmailRequest;
import com.issuemanage.auth.dto.MessageResponse;
import com.issuemanage.auth.dto.ResetPasswordRequest;
import com.issuemanage.auth.dto.VerifyOtpRequest;
import com.issuemanage.auth.model.EmailOtp;
import com.issuemanage.auth.model.OtpPurpose;
import com.issuemanage.auth.model.User;
import com.issuemanage.auth.repository.EmailOtpRepository;
import com.issuemanage.auth.repository.UserRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private final EmailOtpRepository emailOtpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationService emailNotificationService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiry-minutes:10}")
    private long otpExpiryMinutes;

    public OtpService(EmailOtpRepository emailOtpRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      EmailNotificationService emailNotificationService) {
        this.emailOtpRepository = emailOtpRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailNotificationService = emailNotificationService;
    }

    public void sendEmailVerificationOtp(User user) {
        if (user.isEmailVerified()) {
            return;
        }
        String otp = generateOtp();
        saveOtp(user.getEmail(), otp, OtpPurpose.EMAIL_VERIFICATION);
        emailNotificationService.sendOtp(
                user.getEmail(),
                "IMS Email Verification OTP",
                "Your IMS email verification OTP is " + otp + ". It expires in " + otpExpiryMinutes + " minutes.",
                otp
        );
    }

    public MessageResponse resendEmailVerificationOtp(EmailRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new IllegalArgumentException("No account found for this email"));

        if (user.isEmailVerified()) {
            return new MessageResponse("Email is already verified");
        }

        sendEmailVerificationOtp(user);
        return new MessageResponse("Verification OTP sent to email");
    }

    public MessageResponse verifyEmailOtp(VerifyOtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found for this email"));

        EmailOtp emailOtp = validateOtp(email, request.getOtp(), OtpPurpose.EMAIL_VERIFICATION);
        emailOtp.setUsed(true);
        emailOtpRepository.save(emailOtp);

        user.setEmailVerified(true);
        userRepository.save(user);
        return new MessageResponse("Email verified successfully");
    }

    public MessageResponse forgotPassword(EmailRequest request) {
        String email = normalizeEmail(request.getEmail());
        userRepository.findByEmail(email).ifPresent(user -> {
            String otp = generateOtp();
            saveOtp(email, otp, OtpPurpose.PASSWORD_RESET);
            emailNotificationService.sendOtp(
                    email,
                    "IMS Password Reset OTP",
                    "Your IMS password reset OTP is " + otp + ". It expires in " + otpExpiryMinutes + " minutes.",
                    otp
            );
        });

        return new MessageResponse("If the account exists, a password reset OTP has been sent");
    }

    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found for this email"));

        EmailOtp emailOtp = validateOtp(email, request.getOtp(), OtpPurpose.PASSWORD_RESET);
        emailOtp.setUsed(true);
        emailOtpRepository.save(emailOtp);

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return new MessageResponse("Password reset successfully");
    }

    private EmailOtp validateOtp(String email, String otp, OtpPurpose purpose) {
        EmailOtp emailOtp = emailOtpRepository.findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new IllegalArgumentException("OTP not found or already used"));

        if (emailOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }
        if (!emailOtp.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        return emailOtp;
    }

    private void saveOtp(String email, String otp, OtpPurpose purpose) {
        EmailOtp entity = new EmailOtp();
        entity.setEmail(email);
        entity.setOtp(otp);
        entity.setPurpose(purpose);
        entity.setUsed(false);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        emailOtpRepository.save(entity);
    }

    private String generateOtp() {
        int value = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(value);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
