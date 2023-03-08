package com.runningwith.domain.users.validator;

import com.runningwith.domain.users.UsersRepository;
import com.runningwith.domain.users.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameFormValidator implements Validator {

    private final UsersRepository usersRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) target;

        if (usersRepository.existsByNickname(nicknameForm.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{nicknameForm.getNickname()}, "이미 사용중인 닉네임 입니다.");
        }
    }
}
