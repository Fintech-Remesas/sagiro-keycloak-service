package com.sagiro.iamservice.infrastructure.web.controller;

import com.sagiro.iamservice.application.dto.UpdateVerificationStatusCommand;
import com.sagiro.iamservice.application.port.input.UpdateVerificationStatusUseCase;
import com.sagiro.iamservice.infrastructure.shared.ApiResponse;
import com.sagiro.iamservice.infrastructure.web.mapper.WebResponseMapper;
import com.sagiro.iamservice.infrastructure.web.request.UpdateVerificationStatusRequest;
import com.sagiro.iamservice.infrastructure.web.response.UserStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/users")
@Tag(name = "Internal Users", description = "Internal-service endpoints for synchronization between IAM and future platform services")
public class InternalUserController {

    private final UpdateVerificationStatusUseCase updateVerificationStatusUseCase;

    public InternalUserController(UpdateVerificationStatusUseCase updateVerificationStatusUseCase) {
        this.updateVerificationStatusUseCase = updateVerificationStatusUseCase;
    }

    @PatchMapping("/{id}/verification-status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update summarized verification state from an internal service",
            description = "This endpoint is designed for trusted internal services such as a future KYC service. IAM stores only the summarized verification projection and does not execute KYC workflows."
    )
    public ResponseEntity<ApiResponse<UserStatusResponse>> updateVerificationStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVerificationStatusRequest request
    ) {
        UserStatusResponse response = WebResponseMapper.toResponse(updateVerificationStatusUseCase.update(
                new UpdateVerificationStatusCommand(
                        id,
                        request.verificationStatus(),
                        request.verificationLevel(),
                        request.verificationUpdatedAt()
                )
        ));
        return ResponseEntity.ok(ApiResponse.success("Verification status updated successfully", response));
    }
}
