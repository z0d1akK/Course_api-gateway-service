package com.innowise.apigateway.saga.registration.dto;

import com.innowise.apigateway.common.constants.messages.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User registration request")
public class RegistrationRequestDto {

    @Schema(example = "user")
    @NotBlank(message = ValidationMessages.USER_NAME_REQUIRED)
    @Size(max = 255, message = ValidationMessages.USER_NAME_SIZE)
    private String name;

    @Schema(example = "user")
    @NotBlank(message = ValidationMessages.USER_SURNAME_REQUIRED)
    @Size(max = 255, message = ValidationMessages.USER_SURNAME_SIZE)
    private String surname;

    @Schema(example = "2005-05-05")
    @NotNull(message = ValidationMessages.USER_BIRTH_DATE_REQUIRED)
    @PastOrPresent(message = ValidationMessages.USER_BIRTH_DATE_PAST)
    private LocalDate birthDate;

    @Schema(example = "user@gmail.com")
    @NotBlank(message = ValidationMessages.USER_EMAIL_REQUIRED)
    @Email(message = ValidationMessages.USER_EMAIL_VALID)
    @Size(max = 255, message = ValidationMessages.USER_EMAIL_SIZE)
    private String email;

    @Schema(example = "testPassw0rd")
    @NotBlank(message = ValidationMessages.USER_PASSWORD_REQUIRED)
    @Size(max = 255, message = ValidationMessages.USER_PASSWORD_SIZE)
    private String password;
}