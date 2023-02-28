package com.runningwith.study;

import com.runningwith.study.form.StudyDescriptionForm;
import com.runningwith.tag.TagEntity;
import com.runningwith.users.UsersEntity;
import com.runningwith.zone.ZoneEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;

    private static void checkIfManager(UsersEntity usersEntity, StudyEntity studyEntity) {
        if (!usersEntity.isManagerOf(studyEntity)) {
            throw new AccessDeniedException("권한을 가지고 있지 않습니다.");
        }
    }

    public StudyEntity createNewStudy(UsersEntity usersEntity, StudyEntity studyEntity) {
        StudyEntity newStudy = studyRepository.save(studyEntity);
        newStudy.addManager(usersEntity);
        return newStudy;
    }

    public StudyEntity getStudyToUpdate(UsersEntity usersEntity, String path) {
        Optional<StudyEntity> optionalStudy = studyRepository.findByPath(path);
        checkIfExistingStudy(optionalStudy);
        StudyEntity studyEntity = optionalStudy.get();
        checkIfManager(usersEntity, studyEntity);
        return studyEntity;
    }

    public StudyEntity getStudyToUpdateTag(UsersEntity usersEntity, String path) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityWithTagsByPath(path);
        checkIfExistingStudy(optionalStudy);
        StudyEntity studyEntity = optionalStudy.get();
        checkIfManager(usersEntity, studyEntity);
        return studyEntity;
    }

    public StudyEntity getStudyToUpdateZone(UsersEntity usersEntity, String path) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityWithZonesByPath(path);
        checkIfExistingStudy(optionalStudy);
        StudyEntity studyEntity = optionalStudy.get();
        checkIfManager(usersEntity, studyEntity);
        return studyEntity;
    }

    public void checkIfExistingStudy(Optional<StudyEntity> optionalStudy) {
        optionalStudy.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));
    }

    public Set<TagEntity> getStudyTags(StudyEntity studyEntity) {
        return studyEntity.getTags();
    }

    public Set<ZoneEntity> getStudyZones(StudyEntity studyEntity) {
        return studyEntity.getZones();
    }

    public void updateStudyDescription(StudyEntity studyEntity, StudyDescriptionForm form) {
        studyEntity.updateDescription(form.getShortDescription(), form.getFullDescription());
    }

    public void enableStudyBanner(StudyEntity studyEntity) {
        studyEntity.updateUseBanner(true);
    }

    public void disableStudyBanner(StudyEntity studyEntity) {
        studyEntity.updateUseBanner(false);
    }

    public void updateStudyBannerImage(StudyEntity studyEntity, String bannerImage) {
        studyEntity.updateStudyBannerImage(bannerImage);

    }

    public void addTag(StudyEntity studyEntity, TagEntity tagEntity) {
        studyEntity.getTags().add(tagEntity);
    }

    public void removeTag(StudyEntity studyEntity, TagEntity tagEntity) {
        studyEntity.getTags().remove(tagEntity);
    }

    public void addZone(StudyEntity studyEntity, ZoneEntity zoneEntity) {
        studyEntity.getZones().add(zoneEntity);
    }

    public void removeZone(StudyEntity studyEntity, ZoneEntity zoneEntity) {
        studyEntity.getZones().remove(zoneEntity);
    }

    public StudyEntity publishStudy(UsersEntity usersEntity, String path) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityWithManagersByPath(path);
        checkIfExistingStudy(optionalStudy);
        StudyEntity studyEntity = optionalStudy.get();
        checkIfManager(usersEntity, studyEntity);
        studyEntity.publish();
        return studyEntity;
    }

    public StudyEntity closeStudy(UsersEntity usersEntity, String path) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityWithManagersByPath(path);
        checkIfExistingStudy(optionalStudy);
        StudyEntity studyEntity = optionalStudy.get();
        checkIfManager(usersEntity, studyEntity);
        studyEntity.close();
        return studyEntity;
    }
}
