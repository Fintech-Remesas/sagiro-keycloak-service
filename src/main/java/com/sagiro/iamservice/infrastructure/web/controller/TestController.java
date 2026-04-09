package com.sagiro.iamservice.infrastructure.web.controller;

import com.sagiro.iamservice.infrastructure.security.ApplicationPrincipal;
import com.sagiro.iamservice.infrastructure.security.JwtApplicationPrincipalAdapter;
import com.sagiro.iamservice.infrastructure.shared.ApiResponse;
import com.sagiro.iamservice.infrastructure.web.response.TestEndpointResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
@Tag(name = "Technical Tests", description = "Technical endpoints to validate service startup, OpenAPI visibility, JWT validation, and role-based authorization")
public class TestController {

    private final JwtApplicationPrincipalAdapter jwtApplicationPrincipalAdapter;

    public TestController(JwtApplicationPrincipalAdapter jwtApplicationPrincipalAdapter) {
        this.jwtApplicationPrincipalAdapter = jwtApplicationPrincipalAdapter;
    }

    @GetMapping("/ping")
    @Operation(summary = "Public ping endpoint", description = "Verifies that the service is up without requiring authentication.")
    public ResponseEntity<ApiResponse<TestEndpointResponse>> ping() {
        return ResponseEntity.ok(ApiResponse.success("IAM service is reachable", new TestEndpointResponse("pong", null, null, null)));
    }

    @GetMapping("/authenticated")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Authenticated test endpoint", description = "Validates that a JWT issued by Keycloak is accepted by the resource server.")
    public ResponseEntity<ApiResponse<TestEndpointResponse>> authenticated(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Authenticated access granted", toResponse(authentication, "authenticated")));
    }

    @GetMapping("/customer")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Customer role test endpoint", description = "Validates role-based access for ROLE_CUSTOMER.")
    public ResponseEntity<ApiResponse<TestEndpointResponse>> customer(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Customer access granted", toResponse(authentication, "customer")));
    }

    @GetMapping("/admin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Admin role test endpoint", description = "Validates role-based access for ROLE_ADMIN.")
    public ResponseEntity<ApiResponse<TestEndpointResponse>> admin(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Admin access granted", toResponse(authentication, "admin")));
    }

    @GetMapping("/service")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Service role test endpoint", description = "Validates role-based access for ROLE_SERVICE.")
    public ResponseEntity<ApiResponse<TestEndpointResponse>> service(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Service-to-service access granted", toResponse(authentication, "service")));
    }

    private TestEndpointResponse toResponse(Authentication authentication, String message) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return new TestEndpointResponse(message, null, null, null);
        }
        ApplicationPrincipal principal = jwtApplicationPrincipalAdapter.fromJwt(jwt);
        return new TestEndpointResponse(message, principal.subject(), principal.preferredUsername(), principal.roles());
    }
}
