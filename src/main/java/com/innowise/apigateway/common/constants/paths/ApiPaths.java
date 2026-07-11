package com.innowise.apigateway.common.constants.paths;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiPaths {

    public static final String REGISTER = "/api/users/register";

    public static final String LOGIN = "/api/auth/token";

    public static final String REFRESH = "/api/auth/refresh";

    public static final String ITEMS = "/api/items";

    public static final String ITEMS_BY_ID = "/api/items/";
}