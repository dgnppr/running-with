package com.runningwith.domain.users;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.runningwith.infra.utils.CustomStringUtils.WITH_USER_NICKNAME;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithUserSecurityContextFactory.class)
public @interface WithUser {
    String value() default WITH_USER_NICKNAME;
}
