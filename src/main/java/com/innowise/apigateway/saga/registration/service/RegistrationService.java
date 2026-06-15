package com.innowise.apigateway.saga.registration.service;

import com.innowise.apigateway.saga.registration.dto.RegistrationRequestDto;
import com.innowise.apigateway.saga.registration.dto.RegistrationResponseDto;
import reactor.core.publisher.Mono;

public interface RegistrationService {

    Mono<RegistrationResponseDto> register(RegistrationRequestDto request);
}