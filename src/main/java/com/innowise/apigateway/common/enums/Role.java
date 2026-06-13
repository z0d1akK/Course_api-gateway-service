package com.innowise.apigateway.common.enums;

import lombok.Getter;

@Getter
public enum Role {

    ROLE_ADMIN("Administrator"),
    ROLE_USER("User");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }
}