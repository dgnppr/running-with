package com.runningwith.modules.study.factory;

import com.runningwith.modules.study.form.StudyForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyEntityFactory {

    public StudyForm createStudyForm(String path, String title, String shortDescription, String fullDescription) {
        return new StudyForm(path, title, shortDescription, fullDescription);
    }
}
