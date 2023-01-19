package com.runningwith.users;

import com.runningwith.account.AccountRepository;
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
        return null;
    }

}
