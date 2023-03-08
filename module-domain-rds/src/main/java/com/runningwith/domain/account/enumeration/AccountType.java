package com.runningwith.domain.account.enumeration;

import lombok.Getter;

/**
 * Enumeration Description
 * <p><p>
 * ADMIN: Accounts with administrator privileges
 * <p>
 * USERS: Accounts with authenticated user privileges
 * <p>
 * ANONYMOUS: Accounts with anonymous user privileges
 */

@Getter
public enum AccountType {
    ADMIN("ROLE_ADMIN"), USERS("ROLE_USER"), ANONYMOUS("ROLE_ANONYMOUS");

    private String role;

    AccountType(String role) {
        this.role = role;
    }
}
