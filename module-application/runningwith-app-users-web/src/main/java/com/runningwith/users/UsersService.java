package com.runningwith.users;

import com.runningwith.account.AccountEntity;
import com.runningwith.account.AccountRepository;
import com.runningwith.account.AccountType;
import com.runningwith.config.AppMessages;
import com.runningwith.config.AppProperties;
import com.runningwith.mail.EmailMessage;
import com.runningwith.mail.EmailService;
import com.runningwith.tag.TagEntity;
import com.runningwith.users.form.Notifications;
import com.runningwith.users.form.PasswordForm;
import com.runningwith.users.form.Profile;
import com.runningwith.users.form.SignUpForm;
import com.runningwith.zone.ZoneEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UsersService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;
    private final AppMessages appMessages;
    private final TemplateEngine templateEngine;

    public UsersEntity processNewUsers(SignUpForm signUpForm) {
        UsersEntity newUsersEntity = saveNewUsers(signUpForm);
        newUsersEntity.generateEmailCheckToken();
        sendSignUpConfirmEmail(newUsersEntity);
        return newUsersEntity;
    }

    public void sendSignUpConfirmEmail(UsersEntity newUsersEntity) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newUsersEntity.getEmailCheckToken() + "&email=" + newUsersEntity.getEmail());
        context.setVariable("nickname", newUsersEntity.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", appMessages.getDomainName() + " 회원가입을 완료하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newUsersEntity.getEmail())
                .subject(appMessages.getDomainName() + " 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    private UsersEntity saveNewUsers(SignUpForm signUpForm) {
        UsersEntity usersEntity = UsersEntity.builder()
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .tags(new HashSet<>())
                .zones(new HashSet<>())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .emailCheckToken(UUID.randomUUID().toString())
                .emailCheckTokenGeneratedAt(LocalDateTime.now())
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .studyCreatedByEmail(false)
                .studyEnrollmentResultByEmail(false)
                .studyUpdatedByEmail(false)
                .accountEntity(new AccountEntity(AccountType.USERS))
                .build();
        accountRepository.save(usersEntity.getAccountEntity());
        usersRepository.save(usersEntity);
        return usersEntity;
    }

    public void completeSignUp(UsersEntity usersEntity) {
        usersEntity.completeSignUp();
    }

    public void login(UsersEntity usersEntity, HttpServletRequest request, HttpServletResponse response) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        UsersContext usersContext = new UsersContext(usersEntity);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                usersContext, usersEntity.getPassword(), usersContext.getAuthorities());
        securityContext.setAuthentication(token);
        securityContextRepository.saveContext(securityContext, request, response);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<UsersEntity> optionalUsers = usersRepository.findByEmail(email);

        if (optionalUsers.isEmpty()) {
            throw new UsernameNotFoundException(email);
        }

        UsersEntity usersEntity = optionalUsers.get();

        return new UsersContext(usersEntity);

    }

    public void updateProfile(UsersEntity usersEntity, Profile profile) {
        usersEntity.updateProfile(profile.getBio(), profile.getProfileUrl(), profile.getOccupation(), profile.getLocation(), profile.getProfileImage());
        usersRepository.save(usersEntity);
    }

    public void updatePassword(UsersEntity usersEntity, PasswordForm passwordForm) {
        String encodedNewPassword = passwordEncoder.encode(passwordForm.getNewPassword());
        usersEntity.updatePassword(encodedNewPassword);
        usersRepository.save(usersEntity);
    }

    public void updateNotifications(UsersEntity usersEntity, Notifications notifications) {
        usersEntity.updateNotifications(
                notifications.isStudyCreatedByEmail(),
                notifications.isStudyCreatedByWeb(),
                notifications.isStudyEnrollmentResultByEmail(),
                notifications.isStudyEnrollmentResultByWeb(),
                notifications.isStudyUpdatedByEmail(),
                notifications.isStudyUpdatedByWeb());
        usersRepository.save(usersEntity);
    }

    public void updateNickname(UsersEntity usersEntity, String nickname) {
        usersEntity.updateNickname(nickname);
        usersRepository.save(usersEntity);
    }

    public void sendLoginLink(UsersEntity usersEntity) {
        usersEntity.generateEmailCheckToken();
        EmailMessage message = EmailMessage.builder()
                .to(usersEntity.getEmail())
                .subject("RunningWith@ login link")
                .message("/login-by-email?token=" + usersEntity.getEmailCheckToken() + "&email=" + usersEntity.getEmail())
                .build();
        emailService.sendEmail(message);
    }

    public Set<TagEntity> getTags(UsersEntity usersEntity) {
        Optional<UsersEntity> byId = usersRepository.findById(usersEntity.getId());
        return byId.orElseThrow().getTags();
    }

    public void addTag(UsersEntity usersEntity, TagEntity tagEntity) {
        Optional<UsersEntity> byId = usersRepository.findById(usersEntity.getId());
        byId.ifPresent(user -> user.getTags().add(tagEntity));
    }

    public void removeTag(UsersEntity usersEntity, TagEntity tagEntity) {
        Optional<UsersEntity> byId = usersRepository.findById(usersEntity.getId());
        byId.filter(user -> user.getTags().remove(tagEntity));
    }

    public Set<ZoneEntity> getZones(UsersEntity usersEntity) {
        Optional<UsersEntity> byId = usersRepository.findById(usersEntity.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZone(UsersEntity usersEntity, ZoneEntity zoneEntity) {
        Optional<UsersEntity> byId = usersRepository.findById(usersEntity.getId());
        byId.ifPresent(user -> user.getZones().add(zoneEntity));
    }

    public void removeZone(UsersEntity usersEntity, ZoneEntity zoneEntity) {
        Optional<UsersEntity> byId = usersRepository.findById(usersEntity.getId());
        byId.ifPresent(user -> user.getZones().remove(zoneEntity));
    }
}
