package com.stc.apm.constants;

public class ConfigurationConstants {
    public static final String REGEX_FOR_VALIDATION_USERNAME = "^[A-Za-z0-9]{6,30}$";
    public static final String REGEX_FOR_VALIDATION_PASSWORD = "^[A-Za-z0-9.!@#$%^&*()_+-=;:.,]{6,30}$";
    public static final String REGEX_FOR_VALIDATION_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";

    public static final String ROLE_USER = "ROLE_USER";

    public static final int DEFAULT_VALUE_LOGIN_ATTEMPTS_FAILED = 0;
}
