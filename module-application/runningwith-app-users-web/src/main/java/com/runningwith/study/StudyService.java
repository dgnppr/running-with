package com.runningwith.study;

import com.runningwith.study.form.StudyDescriptionForm;
import com.runningwith.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;

    public StudyEntity createNewStudy(UsersEntity usersEntity, StudyEntity studyEntity) {
        StudyEntity newStudy = studyRepository.save(studyEntity);
        newStudy.addManager(usersEntity);
        return newStudy;
    }

    public StudyEntity getStudyToUpdate(UsersEntity usersEntity, String path) {
        StudyEntity studyEntity = this.getStudy(path);

        if (!usersEntity.isManagerOf(studyEntity)) {
            throw new AccessDeniedException("권한을 가지고 있지 않습니다.");
        }

        return studyEntity;
    }

    private StudyEntity getStudy(String path) {
        return studyRepository.findByPath(path).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));
    }

    public void updateStudyDescription(StudyEntity studyEntity, StudyDescriptionForm form) {
        studyEntity.updateDescription(form.getShortDescription(), form.getFullDescription());
    }
}
