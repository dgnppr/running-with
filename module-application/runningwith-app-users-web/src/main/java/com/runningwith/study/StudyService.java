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

import static com.runningwith.study.form.StudyForm.VALID_PATH_PATTERN;

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

    public void publishStudy(UsersEntity usersEntity, String path) {
        StudyEntity studyEntity = getStudyToUpdateStatus(usersEntity, path);
        studyEntity.publish();
    }

    public void closeStudy(UsersEntity usersEntity, String path) {
        StudyEntity studyEntity = getStudyToUpdateStatus(usersEntity, path);
        studyEntity.close();
    }

    public StudyEntity getStudyToUpdateStatus(UsersEntity usersEntity, String path) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityWithManagersByPath(path);
        checkIfExistingStudy(optionalStudy);
        StudyEntity studyEntity = optionalStudy.get();
        checkIfManager(usersEntity, studyEntity);
        return studyEntity;
    }

    public void startStudyRecruit(StudyEntity studyEntity) {
        studyEntity.startRecruit();
    }

    public void stopStudyRecruit(StudyEntity studyEntity) {
        studyEntity.stopRecruit();
    }

    public void updateStudyPath(StudyEntity studyEntity, String newPath) {
        studyEntity.updatePath(newPath);
    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches(VALID_PATH_PATTERN)) {
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

    // TODO 모임 진행 했던 스터디 제거 방지 로직 추가
    public void removeStudy(StudyEntity studyEntity) {
        if (studyEntity.isRemovable()) {
            studyRepository.delete(studyEntity);
        } else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    }

    public void removeMember(String path, UsersEntity usersEntity) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityWithMembersByPath(path);
        checkIfExistingStudy(optionalStudy);
        StudyEntity studyEntity = optionalStudy.get();
        studyEntity.getMembers().remove(usersEntity);
    }

    public void addMember(String path, UsersEntity usersEntity) {
        Optional<StudyEntity> optionalStudy = studyRepository.findStudyEntityWithMembersByPath(path);
        checkIfExistingStudy(optionalStudy);
        StudyEntity studyEntity = optionalStudy.get();
        studyEntity.getMembers().add(usersEntity);
    }
}
