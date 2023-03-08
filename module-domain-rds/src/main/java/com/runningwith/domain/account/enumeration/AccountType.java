package com.runningwith.domain.account.enumeration;

import lombok.Getter;

@Getter
public enum AccountType {
    ADMIN("ROLE_ADMIN"), USERS("ROLE_USER"), ANONYMOUS("ROLE_ANONYMOUS");

    private String role;

    AccountType(String role) {
        this.role = role;
    }
}
