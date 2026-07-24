package com.issuemanage.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class CaptchaService {

    @Value("${google.recaptcha.secret}")
    private String captchaSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public boolean isValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            String url = String.format("%s?secret=%s&response=%s", RECAPTCHA_VERIFY_URL, captchaSecret, token);
            @SuppressWarnings({ "unchecked", "null" })
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);
            
            if (response == null) return false;
            
            Boolean success = (Boolean) response.get("success");
            return success != null && success;
        } catch (Exception e) {
            return false;
        }
    }
}
