package com.innowise.apigateway.client.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateTokenRequestDto {

    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiJ9eyJhbGciOiJIUzI1NiJ9eyJhbGciOiJIUzI1NiJ9")
    @NotBlank
    private String token;
}