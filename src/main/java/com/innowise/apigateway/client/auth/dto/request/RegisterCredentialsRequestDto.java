package com.innowise.apigateway.client.auth.dto.request;

import lombok.*;
import com.innowise.apigateway.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterCredentialsRequestDto {

    @Schema(description = "User identifier", example = "e73dcc73-e1db-4c3a-9246-0e1c2de79074")
    @NotNull
    private UUID userId;

    @Schema(description = "User email", example = "user@gmail.com")
    @NotBlank
    @Email
    @Size(max = 255)
    private String login;

    @Schema(description = "User password", example = "qwerty123A")
    @NotBlank
    @Size(max = 255)
    private String password;

    @Schema(description = "User role", example = "ROLE_USER")
    @NotNull
    private Role role;
}
