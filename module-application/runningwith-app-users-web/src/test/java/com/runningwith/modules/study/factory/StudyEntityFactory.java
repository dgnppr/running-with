package com.runningwith.modules.study.factory;

import com.runningwith.modules.study.StudyEntity;
import com.runningwith.modules.study.StudyService;
import com.runningwith.modules.study.form.StudyForm;
import com.runningwith.modules.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyEntityFactory {

    @Autowired
    StudyService studyService;

    public StudyEntity createStudyEntity(UsersEntity usersEntity, StudyEntity studyEntity) {
        return studyService.createNewStudy(usersEntity, studyEntity);
    }

    public StudyForm createStudyForm(String path, String title, String shortDescription, String fullDescription) {
        return new StudyForm(path, title, shortDescription, fullDescription);
    }
}
