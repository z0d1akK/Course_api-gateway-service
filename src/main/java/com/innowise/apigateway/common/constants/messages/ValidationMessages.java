package com.innowise.apigateway.common.constants.messages;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationMessages {

    public static final String USER_NAME_REQUIRED = "Name is required";
    public static final String USER_NAME_SIZE = "Name must be less than 255 characters";

    public static final String USER_SURNAME_REQUIRED = "Surname is required";
    public static final String USER_SURNAME_SIZE = "Surname must be less than 255 characters";

    public static final String USER_BIRTH_DATE_REQUIRED = "Birth date is required";
    public static final String USER_BIRTH_DATE_PAST = "Birth date must be in the past or present";

    public static final String USER_EMAIL_REQUIRED = "Email is required";
    public static final String USER_EMAIL_VALID = "Email should be valid";
    public static final String USER_EMAIL_SIZE = "Email must be less than 255 characters";

    public static final String USER_PASSWORD_REQUIRED = "Password is required";
    public static final String USER_PASSWORD_SIZE = "Password must be less than 255 characters";

    public static final String TOKEN_REQUIRED = "Token is required";
    public static final String REFRESH_TOKEN_REQUIRED = "Refresh token is required";
}
