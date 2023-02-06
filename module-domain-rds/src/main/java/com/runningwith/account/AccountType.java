package com.runningwith.account;

import lombok.Getter;

@Getter
public enum AccountType {
    ADMIN("ROLE_ADMIN"), USERS("ROLE_USERS"), ANONYMOUS("ROLE_ANONYMOUS");

    private String role;

    AccountType(String role) {
        this.role = role;
    }
}
