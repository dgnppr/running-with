package com.runningwith.domain.study;

import com.runningwith.domain.study.event.StudyCreatedEvent;
import com.runningwith.domain.study.event.StudyUpdatedEvent;
import com.runningwith.domain.study.form.StudyDescriptionForm;
import com.runningwith.domain.study.form.StudyForm;
import com.runningwith.domain.tag.TagEntity;
import com.runningwith.domain.users.UsersEntity;
import com.runningwith.domain.zone.ZoneEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;


    public StudyEntity getStudy(String path) {
        Optional<StudyEntity> optionalStudy = studyRepository.findByPath(path);
        StudyEntity studyEntity = checkIfExistingStudy(optionalStudy);
        return studyEntity;
    }

    public StudyEntity createNewStudy(UsersEntity usersEntity, StudyEntity studyEntity) {
        StudyEntity newStudy = studyRepository.save(studyEntity);
        newStudy.addManager(usersEntity);
        return newStudy;
    }

    public StudyEntity getStudyToUpdate(UsersEntity usersEntity, String path) {
        Optional<StudyEntity> optionalStudy = studyRepository.findByPath(path);
        StudyEntity studyEntity = checkIfExistingStudy(optionalStudy);
        checkIfManager(usersEntity, studyEntity);
        return studyEntity;
    }

    public StudyEntity getStudyToUpdateTag(UsersEntity usersEntity, String path) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityWithTagsByPath(path);
        StudyEntity studyEntity = checkIfExistingStudy(optionalStudy);
        checkIfManager(usersEntity, studyEntity);
        return studyEntity;
    }

    public StudyEntity getStudyToUpdateZone(UsersEntity usersEntity, String path) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityWithZonesByPath(path);
        StudyEntity studyEntity = checkIfExistingStudy(optionalStudy);
        checkIfManager(usersEntity, studyEntity);
        return studyEntity;
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

    public void publishStudy(UsersEntity usersEntity, String path) {
        StudyEntity studyEntity = getStudyToUpdateStatus(usersEntity, path);
        studyEntity.publish();
        eventPublisher.publishEvent(new StudyCreatedEvent(studyEntity));
    }

    public void closeStudy(UsersEntity usersEntity, String path) {
        StudyEntity studyEntity = getStudyToUpdateStatus(usersEntity, path);
        studyEntity.close();
        eventPublisher.publishEvent(new StudyUpdatedEvent(studyEntity, "스터디를 종료했습니다."));
    }

    public StudyEntity getStudyToUpdateStatus(UsersEntity usersEntity, String path) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityWithManagersByPath(path);
        StudyEntity studyEntity = checkIfExistingStudy(optionalStudy);
        checkIfManager(usersEntity, studyEntity);
        return studyEntity;
    }

    public StudyEntity getStudyToEnroll(String path) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityOnlyByPath(path);
        StudyEntity studyEntity = checkIfExistingStudy(optionalStudy);
        return studyEntity;
    }

    public void startStudyRecruit(StudyEntity studyEntity) {
        studyEntity.startRecruit();
        eventPublisher.publishEvent(new StudyUpdatedEvent(studyEntity, "스터디 참여자 모집을 시작합니다."));
    }

    public void stopStudyRecruit(StudyEntity studyEntity) {
        studyEntity.stopRecruit();
        eventPublisher.publishEvent(new StudyUpdatedEvent(studyEntity, "스터디 참여자 모집을 종료합니다."));
    }

    public void updateStudyPath(StudyEntity studyEntity, String newPath) {
        studyEntity.updatePath(newPath);
    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches(StudyForm.VALID_PATH_PATTERN)) {
            return false;
        }

        return !studyRepository.existsByPath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void updateStudyTitle(StudyEntity studyEntity, String newTitle) {
        studyEntity.updateTitle(newTitle);
    }

    public void removeStudy(StudyEntity studyEntity) {
        if (studyEntity.isRemovable()) {
            studyRepository.delete(studyEntity);
        } else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    }

    public void removeMember(String path, UsersEntity usersEntity) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityWithMembersByPath(path);
        StudyEntity studyEntity = checkIfExistingStudy(optionalStudy);
        studyEntity.removeMember(usersEntity);
    }

    public void addMember(String path, UsersEntity usersEntity) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityWithMembersByPath(path);
        StudyEntity studyEntity = checkIfExistingStudy(optionalStudy);
        studyEntity.addMember(usersEntity);
    }

    private void checkIfManager(UsersEntity usersEntity, StudyEntity studyEntity) {
        if (!studyEntity.isManager(usersEntity)) {
            throw new AccessDeniedException("권한을 가지고 있지 않습니다.");
        }
    }

    private StudyEntity checkIfExistingStudy(Optional<StudyEntity> optionalStudy) {
        return optionalStudy.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));
    }
}
