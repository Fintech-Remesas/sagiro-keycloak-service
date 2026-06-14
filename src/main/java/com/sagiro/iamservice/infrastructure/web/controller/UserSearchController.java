package com.sagiro.iamservice.infrastructure.web.controller;

import com.sagiro.iamservice.application.dto.UserSearchPage;
import com.sagiro.iamservice.application.port.input.SearchUsersUseCase;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.infrastructure.shared.ApiResponse;
import com.sagiro.iamservice.infrastructure.web.mapper.WebResponseMapper;
import com.sagiro.iamservice.infrastructure.web.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Search", description = "Endpoints for finding users safely")
@SecurityRequirement(name = "bearerAuth")
public class UserSearchController {

    private final SearchUsersUseCase searchUsersUseCase;

    public UserSearchController(SearchUsersUseCase searchUsersUseCase) {
        this.searchUsersUseCase = searchUsersUseCase;
    }

    @GetMapping("/search")
    @Operation(summary = "Search for verified users", description = "Search by name, phone, or id. Returns max 20 results per page.")
    public ResponseEntity<ApiResponse<UserSearchPage>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "name") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        UserSearchPage result = searchUsersUseCase.searchUsers(query, type, page, size);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", result));
    }

    @GetMapping("/{id}/public-profile")
    @Operation(summary = "Get user public profile", description = "Returns limited info for a verified user")
    public ResponseEntity<ApiResponse<UserResponse>> getPublicProfile(@PathVariable UUID id) {
        User user = searchUsersUseCase.getPublicProfile(id)
                .orElseThrow(() -> new com.sagiro.iamservice.application.exception.ResourceNotFoundException("User not found or not verified"));
        
        com.sagiro.iamservice.application.dto.UserView userView = new com.sagiro.iamservice.application.dto.UserView(
            user.getId(), user.getKeycloakUserId(), user.getEmail(), user.getUsername(), user.getPhone(),
            user.getFirstName(), user.getLastName(), user.getAccountStatus(), user.getVerificationStatus(),
            user.getVerificationLevel(), user.getVerificationUpdatedAt(), user.isCanOperate(), user.isEnabled(),
            user.getCreatedAt(), user.getUpdatedAt(), null
        );
        return ResponseEntity.ok(ApiResponse.success("Public profile retrieved", WebResponseMapper.toResponse(userView)));
    }
}
