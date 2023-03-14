package com.runningwith.domain.users;

import com.runningwith.domain.users.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class WithUserSecurityContextFactory implements WithSecurityContextFactory<WithUser> {

    public static final String PASSWORD = "ValidPass123!";
    public static final String EMAIL = "@email.com";
    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SecurityContext createSecurityContext(WithUser withUser) {

        String nickname = withUser.value();

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail(nickname + EMAIL);
        signUpForm.setPassword(passwordEncoder.encode(PASSWORD));
        signUpForm.setNickname(nickname);
        usersService.processNewUsers(signUpForm);

        UserDetails principals = usersService.loadUserByUsername(nickname + EMAIL);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principals, principals.getPassword(), principals.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
