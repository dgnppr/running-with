package com.runningwith.domain.users.form;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import static com.runningwith.infra.utils.DomainUtils.PASSWORD_REGEX;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class PasswordForm {

    @Length(min = 8, max = 50)
    @Pattern(regexp = PASSWORD_REGEX)
    private String newPassword;

    @Length(min = 8, max = 50)
    @Pattern(regexp = PASSWORD_REGEX)
    private String newPasswordConfirm;

}
