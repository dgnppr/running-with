package com.runningwith.study;

import com.runningwith.study.form.StudyDescriptionForm;
import com.runningwith.users.CurrentUser;
import com.runningwith.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    public static final String VIEW_STUDY_SETTINGS_DESCRIPTION = "study/settings/description";
    private final StudyService studyService;

    // TODO 뷰 수정(DRAFT OFF)
    @GetMapping("/description")
    public String studySettingView(@CurrentUser UsersEntity usersEntity, @PathVariable String path, Model model) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        model.addAttribute("user", usersEntity);
        model.addAttribute("study", studyEntity);
        model.addAttribute(new StudyDescriptionForm(studyEntity));
        return VIEW_STUDY_SETTINGS_DESCRIPTION;
    }

}
