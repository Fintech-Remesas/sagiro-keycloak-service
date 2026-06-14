package com.sagiro.iamservice.infrastructure.web.controller;

import com.sagiro.iamservice.application.dto.AddBankAccountCommand;
import com.sagiro.iamservice.application.port.input.GetCurrentUserUseCase;
import com.sagiro.iamservice.application.port.input.ManageBankAccountsUseCase;
import com.sagiro.iamservice.infrastructure.shared.ApiResponse;
import com.sagiro.iamservice.infrastructure.web.mapper.FinancialWebMapper;
import com.sagiro.iamservice.infrastructure.web.request.AddBankAccountRequest;
import com.sagiro.iamservice.infrastructure.web.response.BankAccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users/me/bank-accounts")
@Tag(name = "Bank Accounts", description = "Endpoints for managing receiving bank accounts")
@SecurityRequirement(name = "bearerAuth")
public class BankAccountController {

    private final ManageBankAccountsUseCase manageBankAccountsUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public BankAccountController(ManageBankAccountsUseCase manageBankAccountsUseCase, GetCurrentUserUseCase getCurrentUserUseCase) {
        this.manageBankAccountsUseCase = manageBankAccountsUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @PostMapping
    @Operation(summary = "Add a new bank account", description = "Requires X-Idempotency-Key header. Account number is AES-256 encrypted.")
    public ResponseEntity<ApiResponse<BankAccountResponse>> addBankAccount(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody AddBankAccountRequest request) {

        String userId = getCurrentUserUseCase.getCurrentUser().id().toString();

        AddBankAccountCommand command = new AddBankAccountCommand(
            userId,
            idempotencyKey,
            request.bankName(),
            request.accountNumber(),
            request.accountType(),
            request.currency(),
            request.country(),
            request.alias()
        );

        BankAccountResponse response = FinancialWebMapper.toResponse(manageBankAccountsUseCase.addBankAccount(command));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Bank account added successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get user bank accounts")
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> getBankAccounts() {
        String userId = getCurrentUserUseCase.getCurrentUser().id().toString();
        List<BankAccountResponse> accounts = manageBankAccountsUseCase.getBankAccounts(userId).stream()
                .map(FinancialWebMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Bank accounts retrieved", accounts));
    }

    @DeleteMapping("/{bankAccountId}")
    @Operation(summary = "Delete a bank account", description = "Soft delete.")
    public ResponseEntity<ApiResponse<Void>> deleteBankAccount(@PathVariable UUID bankAccountId) {
        String userId = getCurrentUserUseCase.getCurrentUser().id().toString();
        manageBankAccountsUseCase.deleteBankAccount(userId, bankAccountId);
        return ResponseEntity.ok(ApiResponse.success("Bank account deleted successfully", null));
    }
}
