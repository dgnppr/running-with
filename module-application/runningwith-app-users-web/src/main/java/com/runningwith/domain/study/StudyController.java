package com.runningwith.domain.study;

import com.runningwith.domain.study.form.StudyForm;
import com.runningwith.domain.study.validator.StudyFormValidator;
import com.runningwith.domain.users.CurrentUser;
import com.runningwith.domain.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import static com.runningwith.infra.utils.CustomStringUtils.getEncodedUrl;
import static com.runningwith.infra.utils.WebUtils.REDIRECT;

@Controller
@RequiredArgsConstructor
public class StudyController {

    public static final String URL_NEW_STUDY = "/new-study";
    public static final String VIEW_STUDY_FORM = "study/form";
    public static final String VIEW_STUDY_MEMBERS = "study/members";
    public static final String FORM_STUDY = "studyForm";
    public static final String URL_STUDY_PATH = "/study/";
    public static final String VIEW_STUDY = "study/view";
    public static final String URL_STUDY_MEMBERS = "/members";
    public static final String URL_STUDY_JOIN = "/join";
    public static final String URL_STUDY_LEAVE = "/leave";

    private final StudyFormValidator studyFormValidator;
    private final StudyService studyService;
    private final StudyRepository studyRepository;

    @InitBinder(FORM_STUDY)
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
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(studyForm.getPath());
    }

    @GetMapping(URL_STUDY_PATH + "{path}")
    public String studyView(@CurrentUser UsersEntity usersEntity, @PathVariable String path, Model model) {
        StudyEntity studyEntity = studyRepository.findByPath(path).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));
        model.addAttribute("user", usersEntity);
        model.addAttribute("study", studyEntity);
        return VIEW_STUDY;
    }

    @GetMapping(URL_STUDY_PATH + "{path}" + URL_STUDY_MEMBERS)
    public String studyMembersView(@CurrentUser UsersEntity usersEntity, @PathVariable String path, Model model) {
        StudyEntity studyEntity = studyRepository.findByPath(path).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));
        model.addAttribute("user", usersEntity);
        model.addAttribute("study", studyEntity);
        return VIEW_STUDY_MEMBERS;
    }

    @GetMapping(URL_STUDY_PATH + "{path}" + URL_STUDY_JOIN)
    public String joinStudy(@CurrentUser UsersEntity usersEntity, @PathVariable String path) {
        studyService.addMember(path, usersEntity);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_MEMBERS;
    }

    @GetMapping(URL_STUDY_PATH + "{path}" + URL_STUDY_LEAVE)
    public String leaveStudy(@CurrentUser UsersEntity usersEntity, @PathVariable String path) {
        studyService.removeMember(path, usersEntity);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_MEMBERS;
    }

}
