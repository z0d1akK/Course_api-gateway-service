package com.innowise.apigateway.saga.registration.mapper;

import com.innowise.apigateway.client.auth.dto.request.RegisterCredentialsRequestDto;
import com.innowise.apigateway.client.auth.dto.response.TokenResponseDto;
import com.innowise.apigateway.client.user.dto.request.CreateUserRequestDto;
import com.innowise.apigateway.saga.registration.dto.RegistrationRequestDto;
import com.innowise.apigateway.saga.registration.dto.RegistrationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RegistrationMapper {

    CreateUserRequestDto toCreateUserRequest(RegistrationRequestDto request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "login", source = "request.email")
    @Mapping(target = "password", source = "request.password")
    @Mapping(target = "role", constant = "ROLE_USER")
    RegisterCredentialsRequestDto toRegisterCredentialsRequest(RegistrationRequestDto request, UUID userId);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "accessToken", source = "token.accessToken")
    @Mapping(target = "refreshToken", source = "token.refreshToken")
    @Mapping(target = "tokenType", source = "token.tokenType")
    RegistrationResponseDto toRegistrationResponse(TokenResponseDto token, UUID userId);
}