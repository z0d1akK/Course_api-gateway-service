package com.innowise.apigateway.saga.registration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Registration response")
public class RegistrationResponseDto {

    @Schema(example = "e73dcc73-e1db-4c3a-9246-0e1c2de79074")
    private UUID userId;

    @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(example = "Bearer")
    private String tokenType;
}