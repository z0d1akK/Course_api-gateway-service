package com.innowise.apigateway.testclasses;

import com.innowise.apigateway.saga.registration.dto.RegistrationRequestDto;

import java.time.LocalDate;

public class RegistrationFactory {

    public static RegistrationRequestDto createValidRequest() {
        return RegistrationRequestDto.builder()
                .name("test")
                .surname("test")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("test@example.com")
                .password("password123")
                .build();
    }

    public static RegistrationRequestDto createRequestWithInvalidEmail() {
        return RegistrationRequestDto.builder()
                .name("test")
                .surname("test")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("invalid-email")
                .password("password123")
                .build();
    }

    public static RegistrationRequestDto createRequestWithEmptyName() {
        return RegistrationRequestDto.builder()
                .name("")
                .surname("test")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("test@example.com")
                .password("password123")
                .build();
    }
}