package com.sagiro.iamservice.infrastructure.config;

import com.sagiro.iamservice.application.port.input.ClaimWelcomeBonusUseCase;
import com.sagiro.iamservice.application.port.input.DeactivateUserFromEventUseCase;
import com.sagiro.iamservice.application.port.input.GetAccessContextUseCase;
import com.sagiro.iamservice.application.port.input.GetCurrentUserUseCase;
import com.sagiro.iamservice.application.port.input.GetUserStatusUseCase;
import com.sagiro.iamservice.application.port.input.LoginUseCase;
import com.sagiro.iamservice.application.port.input.PasswordRecoveryUseCase;
import com.sagiro.iamservice.application.port.input.ProvisionUserFromEventUseCase;
import com.sagiro.iamservice.application.port.input.RegisterUserUseCase;
import com.sagiro.iamservice.application.port.input.SimulateKycVerificationUseCase;
import com.sagiro.iamservice.application.port.input.UpdateUserProfileUseCase;
import com.sagiro.iamservice.application.port.input.UpdateVerificationStatusUseCase;
import com.sagiro.iamservice.application.port.output.BankAccountRepositoryPort;
import com.sagiro.iamservice.application.port.output.CardRepositoryPort;
import com.sagiro.iamservice.application.port.output.CurrentUserProviderPort;
import com.sagiro.iamservice.application.port.output.KeycloakAdminPort;
import com.sagiro.iamservice.application.port.output.LedgerDepositPort;
import com.sagiro.iamservice.application.port.output.PasswordRecoveryPublisherPort;
import com.sagiro.iamservice.application.port.output.UserProfileRepositoryPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.application.service.ClaimWelcomeBonusService;
import com.sagiro.iamservice.application.service.DeactivateUserFromEventService;
import com.sagiro.iamservice.application.service.GetAccessContextService;
import com.sagiro.iamservice.application.service.GetCurrentUserService;
import com.sagiro.iamservice.application.service.GetUserStatusService;
import com.sagiro.iamservice.application.service.LoginService;
import com.sagiro.iamservice.application.service.PasswordRecoveryService;
import com.sagiro.iamservice.application.service.ProvisionUserFromEventService;
import com.sagiro.iamservice.application.service.RegisterUserService;
import com.sagiro.iamservice.application.service.SimulateKycVerificationService;
import com.sagiro.iamservice.application.service.UpdateCurrentUserProfileService;
import com.sagiro.iamservice.application.service.UpdateVerificationStatusService;
import com.sagiro.iamservice.infrastructure.adapter.out.http.LedgerDepositHttpAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

@Configuration
public class ApplicationBeanConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    RestTemplate restTemplate(org.springframework.boot.web.client.RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(java.time.Duration.ofSeconds(5))
                .setReadTimeout(java.time.Duration.ofSeconds(15))
                .build();
    }

    @Bean
    LedgerDepositPort ledgerDepositPort(
            RestTemplate restTemplate,
            @Value("${app.ledger.base-url:http://localhost:8085}") String ledgerServiceBaseUrl
    ) {
        return new LedgerDepositHttpAdapter(restTemplate, ledgerServiceBaseUrl);
    }

    @Bean
    RegisterUserUseCase registerUserUseCase(
            UserRepositoryPort userRepositoryPort,
            UserProfileRepositoryPort userProfileRepositoryPort,
            KeycloakAdminPort keycloakAdminPort,
            Clock clock
    ) {
        return new RegisterUserService(userRepositoryPort, userProfileRepositoryPort, keycloakAdminPort, clock);
    }

    @Bean
    GetCurrentUserUseCase getCurrentUserUseCase(
            CurrentUserProviderPort currentUserProviderPort,
            UserRepositoryPort userRepositoryPort,
            UserProfileRepositoryPort userProfileRepositoryPort
    ) {
        return new GetCurrentUserService(currentUserProviderPort, userRepositoryPort, userProfileRepositoryPort);
    }

    @Bean
    UpdateUserProfileUseCase updateUserProfileUseCase(
            CurrentUserProviderPort currentUserProviderPort,
            UserRepositoryPort userRepositoryPort,
            UserProfileRepositoryPort userProfileRepositoryPort,
            Clock clock
    ) {
        return new UpdateCurrentUserProfileService(currentUserProviderPort, userRepositoryPort, userProfileRepositoryPort, clock);
    }

    @Bean
    GetAccessContextUseCase getAccessContextUseCase(
            CurrentUserProviderPort currentUserProviderPort,
            UserRepositoryPort userRepositoryPort
    ) {
        return new GetAccessContextService(currentUserProviderPort, userRepositoryPort);
    }

    @Bean
    GetUserStatusUseCase getUserStatusUseCase(UserRepositoryPort userRepositoryPort) {
        return new GetUserStatusService(userRepositoryPort);
    }

    @Bean
    UpdateVerificationStatusUseCase updateVerificationStatusUseCase(UserRepositoryPort userRepositoryPort, Clock clock) {
        return new UpdateVerificationStatusService(userRepositoryPort, clock);
    }

    @Bean
    ProvisionUserFromEventUseCase provisionUserFromEventUseCase(
            UserRepositoryPort userRepositoryPort,
            UserProfileRepositoryPort userProfileRepositoryPort,
            Clock clock
    ) {
        return new ProvisionUserFromEventService(userRepositoryPort, userProfileRepositoryPort, clock);
    }

    @Bean
    DeactivateUserFromEventUseCase deactivateUserFromEventUseCase(
            UserRepositoryPort userRepositoryPort,
            KeycloakAdminPort keycloakAdminPort,
            Clock clock
    ) {
        return new DeactivateUserFromEventService(userRepositoryPort, keycloakAdminPort, clock);
    }

    @Bean
    SimulateKycVerificationUseCase simulateKycVerificationUseCase(
            CurrentUserProviderPort currentUserProviderPort,
            UserRepositoryPort userRepositoryPort,
            Clock clock
    ) {
        return new SimulateKycVerificationService(currentUserProviderPort, userRepositoryPort, clock);
    }

    @Bean
    LoginUseCase loginUseCase(KeycloakAdminPort keycloakAdminPort) {
        return new LoginService(keycloakAdminPort);
    }

    @Bean
    PasswordRecoveryUseCase passwordRecoveryUseCase(
            UserRepositoryPort userRepositoryPort,
            KeycloakAdminPort keycloakAdminPort,
            PasswordRecoveryPublisherPort passwordRecoveryPublisherPort,
            Clock clock,
            @Value("${app.password-reset.token-ttl-minutes:15}") long tokenTtlMinutes
    ) {
        return new PasswordRecoveryService(
                userRepositoryPort,
                keycloakAdminPort,
                passwordRecoveryPublisherPort,
                clock,
                tokenTtlMinutes
        );
    }

    @Bean
    ClaimWelcomeBonusUseCase claimWelcomeBonusUseCase(
            UserRepositoryPort userRepositoryPort,
            CardRepositoryPort cardRepositoryPort,
            BankAccountRepositoryPort bankAccountRepositoryPort,
            LedgerDepositPort ledgerDepositPort,
            Clock clock
    ) {
        return new ClaimWelcomeBonusService(
                userRepositoryPort,
                cardRepositoryPort,
                bankAccountRepositoryPort,
                ledgerDepositPort,
                clock
        );
    }
}

