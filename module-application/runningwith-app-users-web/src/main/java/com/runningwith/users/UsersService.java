package com.runningwith.users;

import com.runningwith.account.AccountEntity;
import com.runningwith.account.AccountRepository;
import com.runningwith.account.AccountType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UsersService {

    private final AccountRepository accountRepository;
    private final UsersRepository usersRepository;

    public UsersEntity processNewUsers() {
        AccountEntity accountEntity = accountRepository.save(new AccountEntity(AccountType.USERS));
        UsersEntity usersEntity = new UsersEntity();
        return usersRepository.save(usersEntity);
    }

}
