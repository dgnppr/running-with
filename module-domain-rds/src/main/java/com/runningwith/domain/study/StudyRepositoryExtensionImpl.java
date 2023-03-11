package com.runningwith.domain.study;

import com.querydsl.jpa.JPQLQuery;
import com.runningwith.domain.tag.QTagEntity;
import com.runningwith.domain.users.QUsersEntity;
import com.runningwith.domain.zone.QZoneEntity;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension {

    public StudyRepositoryExtensionImpl() {
        super(StudyEntity.class);
    }

    @Override
    public List<StudyEntity> findByKeyword(String keyword) {
        QStudyEntity studyEntity = QStudyEntity.studyEntity;

        JPQLQuery<StudyEntity> query = from(studyEntity).where(studyEntity.published.isTrue()
                        .and(studyEntity.title.containsIgnoreCase(keyword))
                        .or(studyEntity.tags.any().title.containsIgnoreCase(keyword))
                        .or(studyEntity.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(studyEntity.tags, QTagEntity.tagEntity).fetchJoin()
                .leftJoin(studyEntity.zones, QZoneEntity.zoneEntity).fetchJoin()
                .leftJoin(studyEntity.members, QUsersEntity.usersEntity).fetchJoin()
                .distinct();
        
        return query.fetch();
    }
}
