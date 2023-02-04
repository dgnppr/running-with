package com.runningwith.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
//@Profile(value = {"local","dev"})
@Component
public class ConsoleEmailService implements EmailService {
    @Override
    public void sendEmail(EmailMessage emailMessage) {
        log.info("{}",emailMessage.getMessage());
    }
}
