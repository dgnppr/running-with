package com.runningwith.modules.users.factory;

import com.runningwith.modules.account.AccountEntity;
import com.runningwith.modules.account.AccountRepository;
import com.runningwith.modules.account.enumeration.AccountType;
import com.runningwith.modules.users.UsersEntity;
import com.runningwith.modules.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.runningwith.modules.users.WithUserSecurityContextFactory.EMAIL;
import static com.runningwith.modules.users.WithUserSecurityContextFactory.PASSWORD;

@Component
@RequiredArgsConstructor
public class UsersEntityFactory {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    AccountRepository accountRepository;

    public UsersEntity createUsersEntity(String nickname) {
        UsersEntity usersEntity = UsersEntity.builder()
                .nickname(nickname)
                .email(nickname + EMAIL)
                .password(PASSWORD)
                .emailCheckToken(UUID.randomUUID().toString())
                .emailCheckTokenGeneratedAt(LocalDateTime.now())
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .studyCreatedByEmail(false)
                .studyEnrollmentResultByEmail(false)
                .studyUpdatedByEmail(false)
                .emailCheckTokenGeneratedAt(LocalDateTime.now().minusHours(2))
                .accountEntity(new AccountEntity(AccountType.USERS))
                .build();
        accountRepository.save(usersEntity.getAccountEntity());
        usersRepository.save(usersEntity);

        return usersEntity;
    }

}
