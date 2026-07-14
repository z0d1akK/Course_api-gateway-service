package com.innowise.apigateway.saga.registration.controller;

import com.innowise.apigateway.common.constants.paths.ApiPaths;
import com.innowise.apigateway.common.dto.response.ValidationErrorResponse;
import com.innowise.apigateway.saga.registration.dto.RegistrationRequestDto;
import com.innowise.apigateway.saga.registration.dto.RegistrationResponseDto;
import com.innowise.apigateway.saga.registration.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.REGISTER)
public class RegistrationController {

    private final RegistrationService registrationService;

    @Operation(summary = "Register user",
            description = """
                    Creates user in UserService,
                    creates credentials in AuthService,
                    performs rollback if credentials creation fails,
                    and returns JWT tokens.
                    """
    )
    @ApiResponse(responseCode = "201", description = "User successfully registered")
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RegistrationResponseDto> register(@Valid @RequestBody RegistrationRequestDto request) {
        return registrationService.register(request);
    }
}