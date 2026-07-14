package com.innowise.apigateway.client.auth.dto.response;

import com.innowise.apigateway.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthCredentialResponseDto {

    @Schema(description = "Credential identifier", example = "e73dcc73-e1db-4c3a-9246-0e1c2de79074")
    private UUID id;

    @Schema(description = "User identifier", example = "e73dcc73-e1db-4c3a-9246-0e1c2de79074")
    private UUID userId;

    @Schema(description = "User login", example = "user@gmail.com")
    private String login;

    @Schema(description = "User role", example = "ROLE_USER")
    private Role role;
}