package com.innowise.apigateway.common.constants.paths;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityPaths {

    public static final String LOGIN = "/api/auth/token";

    public static final String REFRESH = "/api/auth/refresh";

    public static final String REGISTER = "/api/register";

    public static final String INTERNAL = "/internal";

    public static final String SWAGGER = "/swagger-ui";

    public static final String API_DOCS = "/api-docs";

    public static final String AUTH_DOCS_PREFIX = "/auth-service";

    public static final String USER_DOCS_PREFIX = "/user-service";

    public static final String ORDER_DOCS_PREFIX = "/order-service";
}