package com.innowise.apigateway.common.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiPaths {

    public static final String API = "/api";

    public static final String REGISTER = "/api/register";

    public static final String AUTH = "/api/auth";

    public static final String LOGIN = "/api/auth/token";

    public static final String REFRESH = "/api/auth/refresh";

    public static final String VALIDATE = "/api/auth/validate";

    public static final String CREDENTIALS = "/api/auth/credentials";

    public static final String USERS = "/api/users/**";

    public static final String PAYMENT_CARDS = "/api/payment-cards/**";

    public static final String ORDERS = "/api/orders/**";

    public static final String INTERNAL_USERS = "/internal/users";

    public static final String INTERNAL_USERS_WITH_ID = "/internal/users/";
}