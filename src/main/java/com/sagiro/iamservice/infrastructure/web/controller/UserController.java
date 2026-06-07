package com.sagiro.iamservice.infrastructure.web.controller;

import com.sagiro.iamservice.application.dto.LoginUserCommand;
import com.sagiro.iamservice.application.dto.RegisterUserCommand;
import com.sagiro.iamservice.application.dto.RequestPasswordRecoveryCommand;
import com.sagiro.iamservice.application.dto.ResetPasswordCommand;
import com.sagiro.iamservice.application.dto.UpdateUserProfileCommand;
import com.sagiro.iamservice.application.port.input.GetAccessContextUseCase;
import com.sagiro.iamservice.application.port.input.GetCurrentUserUseCase;
import com.sagiro.iamservice.application.port.input.GetUserStatusUseCase;
import com.sagiro.iamservice.application.port.input.LoginUseCase;
import com.sagiro.iamservice.application.port.input.PasswordRecoveryUseCase;
import com.sagiro.iamservice.application.port.input.RegisterUserUseCase;
import com.sagiro.iamservice.application.port.input.SimulateKycVerificationUseCase;
import com.sagiro.iamservice.application.port.input.UpdateUserProfileUseCase;
import com.sagiro.iamservice.infrastructure.shared.ApiResponse;
import com.sagiro.iamservice.infrastructure.web.mapper.WebResponseMapper;
import com.sagiro.iamservice.infrastructure.web.request.LoginRequest;
import com.sagiro.iamservice.infrastructure.web.request.PasswordRecoveryRequest;
import com.sagiro.iamservice.infrastructure.web.request.PasswordResetRequest;
import com.sagiro.iamservice.infrastructure.web.request.RegisterUserRequest;
import com.sagiro.iamservice.infrastructure.web.request.UpdateUserProfileRequest;
import com.sagiro.iamservice.infrastructure.web.response.AccessContextResponse;
import com.sagiro.iamservice.infrastructure.web.response.LoginResponse;
import com.sagiro.iamservice.infrastructure.web.response.UserResponse;
import com.sagiro.iamservice.infrastructure.web.response.UserStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Users", description = "IAM endpoints for registration, authentication, self-service profile management, and password recovery")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final GetAccessContextUseCase getAccessContextUseCase;
    private final GetUserStatusUseCase getUserStatusUseCase;
    private final SimulateKycVerificationUseCase simulateKycVerificationUseCase;
    private final PasswordRecoveryUseCase passwordRecoveryUseCase;

    public UserController(
            RegisterUserUseCase registerUserUseCase,
            LoginUseCase loginUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase,
            UpdateUserProfileUseCase updateUserProfileUseCase,
            GetAccessContextUseCase getAccessContextUseCase,
            GetUserStatusUseCase getUserStatusUseCase,
            SimulateKycVerificationUseCase simulateKycVerificationUseCase,
            PasswordRecoveryUseCase passwordRecoveryUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.getAccessContextUseCase = getAccessContextUseCase;
        this.getUserStatusUseCase = getUserStatusUseCase;
        this.simulateKycVerificationUseCase = simulateKycVerificationUseCase;
        this.passwordRecoveryUseCase = passwordRecoveryUseCase;
    }

    // ─────────────────────────────────────────────────────────
    // PUBLIC ENDPOINTS (no Bearer token required)
    // ─────────────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(
            summary = "Register a business user",
            description = """
                    Creates the local IAM user record and provisions the corresponding identity in Keycloak.
                    An optional temporary password can be forwarded to Keycloak; if omitted, Keycloak will require
                    the user to set one on first login (UPDATE_PASSWORD required action).
                    The user starts with accountStatus=REGISTERED and verificationStatus=NOT_STARTED.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error – missing or invalid fields"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict – email or username already taken in IAM or Keycloak")
    })
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

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate a user",
            description = """
                    Authenticates the user through Keycloak's token endpoint using the resource-owner password grant.
                    Returns a full OIDC token pair (access_token + refresh_token). The access token must be included
                    as a Bearer token in all subsequent authenticated requests.
                    **Do not store the refresh token in local storage on the frontend** – use HttpOnly cookies instead.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error – missing credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid username/email or password")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = WebResponseMapper.toResponse(
                loginUseCase.login(new LoginUserCommand(request.usernameOrEmail(), request.password()))
        );
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    // ─────────────────────────────────────────────────────────
    // AUTHENTICATED ENDPOINTS (Bearer token required)
    // ─────────────────────────────────────────────────────────

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get the authenticated user",
            description = "Resolves the current IAM user from the JWT subject (sub claim). Returns the full user record including profile preferences."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authenticated user resolved",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User is not yet provisioned in IAM (edge case during provisioning lag)")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = WebResponseMapper.toResponse(getCurrentUserUseCase.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success("Authenticated user resolved", response));
    }

    @PatchMapping("/me/profile")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update the authenticated user's profile",
            description = """
                    Updates the personal display data and preferences stored locally in IAM.
                    All provided fields are overwritten. Only the fields in this payload are updated;
                    identity fields (email, username) are not modifiable through this endpoint.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not provisioned in IAM")
    })
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
    @Operation(
            summary = "Get the authenticated user's access context",
            description = """
                    Returns the full access context: roles extracted from the JWT, account status,
                    verification projection, and the canOperate flag. Intended for use by the API Gateway
                    and frontend to make authorization decisions without additional round trips.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Access context resolved",
                    content = @Content(schema = @Schema(implementation = AccessContextResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token")
    })
    public ResponseEntity<ApiResponse<AccessContextResponse>> getAccessContext() {
        AccessContextResponse response = WebResponseMapper.toResponse(getAccessContextUseCase.getCurrentAccessContext());
        return ResponseEntity.ok(ApiResponse.success("Access context resolved", response));
    }

    @GetMapping("/{id}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get a user's IAM status by ID",
            description = "Administrative endpoint for compliance agents and services to inspect any user's account state and verification projection. Requires ROLE_ADMIN or ROLE_SERVICE."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User status resolved",
                    content = @Content(schema = @Schema(implementation = UserStatusResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient role – requires ADMIN or SERVICE"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserStatusResponse>> getUserStatus(@PathVariable UUID id) {
        UserStatusResponse response = WebResponseMapper.toResponse(getUserStatusUseCase.getUserStatus(id));
        return ResponseEntity.ok(ApiResponse.success("User status resolved", response));
    }

    @PostMapping("/me/simulate-kyc")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "[MOCK] Simulate KYC verification",
            description = """
                    **FOR TESTING ONLY** – Immediately marks the current user's verification status as VERIFIED.
                    This endpoint is a stub that simulates a successful KYC approval webhook (e.g., from Sumsub).
                    It will be replaced by a real webhook integration in production.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KYC simulated",
                    content = @Content(schema = @Schema(implementation = UserStatusResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not provisioned in IAM")
    })
    public ResponseEntity<ApiResponse<UserStatusResponse>> simulateKyc() {
        UserStatusResponse response = WebResponseMapper.toResponse(simulateKycVerificationUseCase.simulateKycForCurrentUser());
        return ResponseEntity.ok(ApiResponse.success("KYC simulated successfully", response));
    }

    // ─────────────────────────────────────────────────────────
    // PASSWORD RECOVERY (no Bearer token required)
    // ─────────────────────────────────────────────────────────

    @PostMapping("/password-recovery/request")
    @Operation(
            summary = "Request password recovery",
            description = """
                    Initiates the password recovery flow for the given email address.
                    A one-time reset token is generated, stored in the IAM database with a 15-minute TTL,
                    and a `PasswordRecoveryEvent` is published to Kafka topic `password-recovery.requested`.
                    The communication-service (to be created) will consume the event and deliver the token
                    to the user via email or SMS.

                    **Security note:** The response always returns 202 Accepted regardless of whether the email
                    exists, to prevent user enumeration attacks.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Recovery request accepted (email may or may not exist)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error – invalid email format")
    })
    public ResponseEntity<ApiResponse<Void>> requestPasswordRecovery(@Valid @RequestBody PasswordRecoveryRequest request) {
        try {
            passwordRecoveryUseCase.requestRecovery(new RequestPasswordRecoveryCommand(request.email()));
        } catch (Exception ignored) {
            // Swallow all exceptions – return 202 to prevent user enumeration (security best practice)
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("If an account with that email exists, a recovery token has been sent", null));
    }

    @PostMapping("/password-recovery/reset")
    @Operation(
            summary = "Reset password using a recovery token",
            description = """
                    Completes the password recovery flow by applying a new password.
                    The `token` must be the one-time UUID token received by the user from the communication-service.
                    The token is validated for existence and expiry (15-minute TTL by default).
                    On success, the new password is set in Keycloak and the token is invalidated in IAM.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error – missing token or new password"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Token not found or already used"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Token has expired – please request a new recovery link")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordRecoveryUseCase.resetPassword(new ResetPasswordCommand(request.token(), request.newPassword()));
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. You can now log in with your new password.", null));
    }
}
