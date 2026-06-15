package com.innowise.apigateway.common.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Headers {

    public static final String AUTHORIZATION = "Authorization";

    public static final String USER_ID = "X-User-Id";
    public static final String USER_ROLE = "X-User-Role";
    public static final String USER_LOGIN = "X-User-Login";

    public static final String INTERNAL_KEY = "X-Internal-Key";

    public static final String FORWARDED_HOST = "X-Forwarded-Host";
    public static final String FORWARDED_PORT = "X-Forwarded-Port";
    public static final String FORWARDED_PROTO = "X-Forwarded-Proto";
}