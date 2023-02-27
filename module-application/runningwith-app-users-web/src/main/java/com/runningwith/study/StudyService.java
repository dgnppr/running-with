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

import java.util.Set;

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

    // TODO Query Optimize -> getStudyToUpdate 분리
    public StudyEntity getStudyToUpdate(UsersEntity usersEntity, String path) {
        StudyEntity studyEntity = this.getStudy(path);

        if (!usersEntity.isManagerOf(studyEntity)) {
            throw new AccessDeniedException("권한을 가지고 있지 않습니다.");
        }

        return studyEntity;
    }

    public Set<TagEntity> getStudyTags(StudyEntity studyEntity) {
        return studyEntity.getTags();
    }

    public Set<ZoneEntity> getStudyZones(StudyEntity studyEntity) {
        return studyEntity.getZones();
    }


    private StudyEntity getStudy(String path) {
        return studyRepository.findByPath(path).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));
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
}
