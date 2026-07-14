package com.innowise.apigateway.client.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    @Schema(description = "User email", example = "user@gmail.com")
    @NotBlank
    @Email
    @Size(max = 255)
    private String login;

    @Schema(description = "User password", example = "qwerty123A")
    @NotBlank
    @Size
    private String password;
}