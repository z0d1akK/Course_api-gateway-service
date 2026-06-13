package com.innowise.apigateway.common.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessages {

    public static final String UNKNOWN = "Unknown service error";

    public static final String INVALID_TOKEN = "Invalid or expired token";

    public static final String AUTHORIZATION_HEADER_MISSING = "Authorization header is missing";

    public static final String AUTHORIZATION_HEADER_INVALID = "Authorization header format is invalid";

    public static final String USER_CREATION_FAILED = "Failed to create user";

    public static final String CREDENTIALS_CREATION_FAILED = "Failed to create user credentials";

    public static final String TOKEN_VALIDATION_FAILED = "Failed to validate token";

    public static final String REGISTRATION_FAILED = "Registration failed";

    public static final String USER_ROLLBACK_FAILED = "Failed to rollback created user";

    public static final String AUTH_SERVICE_UNAVAILABLE = "Auth service is unavailable";

    public static final String USER_SERVICE_UNAVAILABLE = "User service is unavailable";

    public static final String ACCESS_DENIED = "Access denied";

    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
}