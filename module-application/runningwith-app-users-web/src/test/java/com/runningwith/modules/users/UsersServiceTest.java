package com.runningwith.modules.users;

import com.runningwith.infra.MockMvcTest;
import com.runningwith.modules.account.AccountEntity;
import com.runningwith.modules.account.AccountRepository;
import com.runningwith.modules.account.enumeration.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

@MockMvcTest
class UsersServiceTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsersService usersService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    AccountRepository accountRepository;

    @DisplayName("로그인 성공 - SecurityContextHolder 안에 토큰 저장")
    @Test
    void login_success() throws Exception {
        // given
        AccountEntity accountEntity = new AccountEntity(AccountType.USERS);
        UsersEntity usersEntity = UsersEntity.builder()
                .accountEntity(accountEntity)
                .email("email@email.com")
                .password("goodpassword")
                .nickname("randomename")
                .build();
        usersEntity.generateEmailCheckToken();
        accountRepository.save(accountEntity);
        usersRepository.save(usersEntity);

        // when
        usersService.login(usersEntity, new MockHttpServletRequest(), new MockHttpServletResponse());

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isInstanceOf(UsersContext.class);
    }


}