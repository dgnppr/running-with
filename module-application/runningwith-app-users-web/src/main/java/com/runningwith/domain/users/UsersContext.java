package com.runningwith.domain.users;

import com.runningwith.domain.account.enumeration.AccountType;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class UsersContext extends User {
    private UsersEntity usersEntity;

    public UsersContext(UsersEntity usersEntity) {
        super(usersEntity.getNickname(), usersEntity.getPassword(), List.of(new SimpleGrantedAuthority(AccountType.USERS.getRole())));
        this.usersEntity = usersEntity;
    }
}
