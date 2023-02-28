package com.runningwith.study.form;

import com.runningwith.study.StudyEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.HashSet;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudyForm {

    public static final String VALID_PATH_PATTERN = "^[ㄱ-ㅎ가-힣a-z0-9_-]{2,20}$";

    @NotBlank
    @Length(min = 2, max = 20)
    @Pattern(regexp = VALID_PATH_PATTERN)
    private String path;

    @NotBlank
    @Length(max = 50)
    private String title;

    @NotBlank
    @Length(max = 100)
    private String shortDescription;

    @NotBlank
    private String fullDescription;

    public StudyEntity toEntity() {
        return StudyEntity.builder()
                .path(this.path)
                .title(this.title)
                .shortDescription(this.shortDescription)
                .fullDescription(this.fullDescription)
                .managers(new HashSet<>())
                .members(new HashSet<>())
                .tags(new HashSet<>())
                .zones(new HashSet<>())
                .build();
    }

}

