package com.runningwith.domain.users;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class EmailVerifiedInterceptor implements HandlerInterceptor {

    private final UsersRepository usersRepository;

    // TODO add email check interceptor
}
