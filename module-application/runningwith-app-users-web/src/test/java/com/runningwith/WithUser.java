package com.runningwith;

import org.springframework.core.annotation.AliasFor;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.runningwith.utils.CustomStringUtils.RANDOM_STRING;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithUserSecurityContextFactory.class)
public @interface WithUser {
    String value() default RANDOM_STRING;

    String username() default "";

    String[] roles() default {"USER"};

    String[] authorities() default {};

    String password() default "password";

    @AliasFor(
            annotation = WithSecurityContext.class
    )
    TestExecutionEvent setupBefore() default TestExecutionEvent.TEST_METHOD;
}
