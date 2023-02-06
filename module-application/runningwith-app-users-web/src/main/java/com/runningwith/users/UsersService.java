package com.runningwith.users;

import com.runningwith.account.AccountEntity;
import com.runningwith.account.AccountRepository;
import com.runningwith.account.AccountType;
import com.runningwith.mail.EmailMessage;
import com.runningwith.mail.EmailService;
import com.runningwith.users.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UsersService {

    private final AccountRepository accountRepository;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void processNewUsers(SignUpForm signUpForm) {
        UsersEntity newUsersEntity = saveNewUsers(signUpForm);
        newUsersEntity.generateEmailCheckToken();
        sendSignUpConfirmEmail(newUsersEntity);
    }

    private void sendSignUpConfirmEmail(UsersEntity newUsersEntity) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(newUsersEntity.getEmail())
                .subject("회원 가입 인증")
                .message("/check-email-token?token=" + newUsersEntity.getEmailCheckToken() +
                        "&email=" + newUsersEntity.getEmail())
                .build();
        emailService.sendEmail(emailMessage);
    }

    private UsersEntity saveNewUsers(SignUpForm signUpForm) {
        UsersEntity usersEntity = UsersEntity.builder()
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .emailCheckToken(UUID.randomUUID().toString())
                .emailCheckTokenGeneratedAt(LocalDateTime.now())
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .accountEntity(new AccountEntity(AccountType.USERS))
                .build();
        accountRepository.save(usersEntity.getAccountEntity());
        usersRepository.save(usersEntity);
        return usersEntity;
    }

    public void completeSignUp(UsersEntity usersEntity) {
        usersEntity.updateEmailVerified(true);
        usersEntity.updateJoinedAt(LocalDateTime.now());
        login(usersEntity);
    }

    private void login(UsersEntity usersEntity) {
        UsersContext usersContext = new UsersContext(usersEntity);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                usersContext, usersContext.getPassword(), usersContext.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}
