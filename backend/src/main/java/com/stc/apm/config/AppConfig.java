package com.stc.apm.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class AppConfig {
    @Value("${urls.google_recaptcha_server}")
    private String googleRecaptchaServerUrl;

    @Value("${secrets.google_recaptcha_server}")
    private String googleRecaptchaServerKey;

    private static AppConfig instance;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public static String getGoogleRecaptchaServerUrl() {
        return instance.googleRecaptchaServerUrl;
    }

    public static String getGoogleRecaptchaServerKey() {
        return instance.googleRecaptchaServerKey;
    }
}
