package com.runningwith.study.form;

import com.runningwith.study.StudyEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudyDescriptionForm {

    @NotBlank
    @Length(max = 100)
    private String shortDescription;

    @NotBlank
    private String fullDescription;

    public StudyDescriptionForm(StudyEntity studyEntity) {
        this.shortDescription = studyEntity.getShortDescription();
        this.fullDescription = studyEntity.getFullDescription();
    }
}

