package com.innowise.apigateway.client.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for creating a new user")
public class CreateUserRequestDto {

    @Schema(description = "User first name", example = "name")
    @NotBlank
    @Size(min = 1, max = 255)
    private String name;

    @Schema(description = "User surname", example = "surname")
    @NotBlank
    @Size(min = 1, max = 255)
    private String surname;

    @Schema(description = "User birth date", example = "2005-05-05")
    @NotNull
    @PastOrPresent
    private LocalDate birthDate;

    @Schema(description = "User email", example = "user@gmail.com")
    @NotBlank
    @Email
    @Size
    private String email;
}