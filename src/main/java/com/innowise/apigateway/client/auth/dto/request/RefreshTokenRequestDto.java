package com.innowise.apigateway.client.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequestDto {

    @Schema(description = "Refresh token", example = "cyBhIHZlcnkgc2VjdXJlIHNlY3JldCBrgMzIgYnl0ZXMu")
    @NotBlank
    private String refreshToken;
}