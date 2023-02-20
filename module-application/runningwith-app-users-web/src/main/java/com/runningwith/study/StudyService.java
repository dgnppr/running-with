package com.runningwith.study;

import com.runningwith.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
}
