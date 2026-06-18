package com.innowise.apigateway.common.constants.paths;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityPaths {

    public static final String INTERNAL = "/internal";

    public static final String INTERNAL_USERS = "/internal/users";

    public static final String INTERNAL_USERS_WITH_ID = "/internal/users/";

    public static final String INTERNAL_VALIDATE = "/internal/auth/validate";

    public static final String INTERNAL_CREDENTIALS = "/internal/auth/credentials";

    public static final String SWAGGER = "/swagger-ui";

    public static final String API_DOCS = "/api-docs";

    public static final String AUTH_DOCS_PREFIX = "/auth-service";

    public static final String USER_DOCS_PREFIX = "/user-service";

    public static final String ORDER_DOCS_PREFIX = "/order-service";
}