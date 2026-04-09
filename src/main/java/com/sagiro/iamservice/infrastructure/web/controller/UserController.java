package com.sagiro.iamservice.infrastructure.web.controller;

import com.sagiro.iamservice.application.dto.RegisterUserCommand;
import com.sagiro.iamservice.application.dto.UpdateUserProfileCommand;
import com.sagiro.iamservice.application.port.input.GetAccessContextUseCase;
import com.sagiro.iamservice.application.port.input.GetCurrentUserUseCase;
import com.sagiro.iamservice.application.port.input.GetUserStatusUseCase;
import com.sagiro.iamservice.application.port.input.RegisterUserUseCase;
import com.sagiro.iamservice.application.port.input.UpdateUserProfileUseCase;
import com.sagiro.iamservice.infrastructure.shared.ApiResponse;
import com.sagiro.iamservice.infrastructure.web.mapper.WebResponseMapper;
import com.sagiro.iamservice.infrastructure.web.request.RegisterUserRequest;
import com.sagiro.iamservice.infrastructure.web.request.UpdateUserProfileRequest;
import com.sagiro.iamservice.infrastructure.web.response.AccessContextResponse;
import com.sagiro.iamservice.infrastructure.web.response.UserResponse;
import com.sagiro.iamservice.infrastructure.web.response.UserStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "IAM endpoints for business-user registration, self-profile access, and status projection")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final GetAccessContextUseCase getAccessContextUseCase;
    private final GetUserStatusUseCase getUserStatusUseCase;

    public UserController(
            RegisterUserUseCase registerUserUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase,
            UpdateUserProfileUseCase updateUserProfileUseCase,
            GetAccessContextUseCase getAccessContextUseCase,
            GetUserStatusUseCase getUserStatusUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.getAccessContextUseCase = getAccessContextUseCase;
        this.getUserStatusUseCase = getUserStatusUseCase;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a business user in IAM and provision the identity in Keycloak",
            description = "Creates the local IAM user/profile and invokes Keycloak administrative APIs to create the external identity."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse response = WebResponseMapper.toResponse(registerUserUseCase.register(new RegisterUserCommand(
                request.email(),
                request.username(),
                request.phone(),
                request.firstName(),
                request.lastName(),
                request.country(),
                request.preferredLanguage(),
                request.initialPassword()
        )));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User registered successfully", response));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get the authenticated IAM user", description = "Resolves the current user from the JWT subject emitted by Keycloak.")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = WebResponseMapper.toResponse(getCurrentUserUseCase.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success("Authenticated user resolved", response));
    }

    @PatchMapping("/me/profile")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update the authenticated user's base profile", description = "Updates personal display data and preferences stored by IAM.")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        UserResponse response = WebResponseMapper.toResponse(updateUserProfileUseCase.updateCurrentUserProfile(
                new UpdateUserProfileCommand(
                        request.phone(),
                        request.firstName(),
                        request.lastName(),
                        request.country(),
                        request.preferredLanguage(),
                        request.blockchainVisibilityEnabled(),
                        request.darkModeEnabled()
                )
        ));
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", response));
    }

    @GetMapping("/me/access-context")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current access context", description = "Returns roles, access flags, account status, and the summarized verification projection.")
    public ResponseEntity<ApiResponse<AccessContextResponse>> getAccessContext() {
        AccessContextResponse response = WebResponseMapper.toResponse(getAccessContextUseCase.getCurrentAccessContext());
        return ResponseEntity.ok(ApiResponse.success("Access context resolved", response));
    }

    @GetMapping("/{id}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get a user's IAM status", description = "Administrative endpoint to inspect account state and summarized verification status.")
    public ResponseEntity<ApiResponse<UserStatusResponse>> getUserStatus(@PathVariable UUID id) {
        UserStatusResponse response = WebResponseMapper.toResponse(getUserStatusUseCase.getUserStatus(id));
        return ResponseEntity.ok(ApiResponse.success("User status resolved", response));
    }
}
