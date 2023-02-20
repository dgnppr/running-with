package com.runningwith.study;

import com.runningwith.study.form.StudyForm;
import com.runningwith.study.validator.StudyFormValidator;
import com.runningwith.users.CurrentUser;
import com.runningwith.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.runningwith.utils.WebUtils.REDIRECT;

@Controller
@RequiredArgsConstructor
public class StudyController {

    public static final String URL_NEW_STUDY = "/new-study";
    public static final String VIEW_STUDY_FORM = "study/form";
    public static final String STUDY_FORM = "studyForm";

    private final StudyFormValidator studyFormValidator;
    private final StudyService studyService;

    @InitBinder(STUDY_FORM)
    public void studyFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(studyFormValidator);
    }

    @GetMapping(URL_NEW_STUDY)
    public String newStudyView(@CurrentUser UsersEntity usersEntity, Model model) {
        model.addAttribute("user", usersEntity);
        model.addAttribute(new StudyForm());
        return VIEW_STUDY_FORM;
    }

    @PostMapping(URL_NEW_STUDY)
    public String submitNewStudy(@CurrentUser UsersEntity usersEntity, @Validated StudyForm studyForm, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return VIEW_STUDY_FORM;
        }

        StudyEntity newStudy = studyService.createNewStudy(usersEntity, studyForm.toEntity());
        return REDIRECT + "/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }
}
