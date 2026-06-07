package com.sagiro.iamservice.domain.service;

import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.enums.VerificationStatus;

public final class UserOperationPolicy {

    private UserOperationPolicy() {
    }

    public static boolean canOperate(AccountStatus accountStatus, VerificationStatus verificationStatus, boolean enabled) {
        return enabled
                && (accountStatus == AccountStatus.ACTIVE || accountStatus == AccountStatus.REGISTERED)
                && verificationStatus != VerificationStatus.REJECTED;
    }
}
