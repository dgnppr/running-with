package com.runningwith.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AppMessages {
    private final MessageSource messageSource;

    public String getDomainName() {
        return messageSource.getMessage("domain.name", new Object[]{}, LocaleContextHolder.getLocale());
    }
}
