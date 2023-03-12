package com.runningwith.domain.study;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import com.runningwith.domain.tag.QTagEntity;
import com.runningwith.domain.users.QUsersEntity;
import com.runningwith.domain.zone.QZoneEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension {

    public StudyRepositoryExtensionImpl() {
        super(StudyEntity.class);
    }

    @Override
    public Page<StudyEntity> findByKeyword(String keyword, Pageable pageable) {
        QStudyEntity studyEntity = QStudyEntity.studyEntity;

        JPQLQuery<StudyEntity> query = from(studyEntity).where(studyEntity.published.isTrue()
                        .and(studyEntity.title.containsIgnoreCase(keyword))
                        .or(studyEntity.tags.any().title.containsIgnoreCase(keyword))
                        .or(studyEntity.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(studyEntity.tags, QTagEntity.tagEntity).fetchJoin()
                .leftJoin(studyEntity.zones, QZoneEntity.zoneEntity).fetchJoin()
                .leftJoin(studyEntity.members, QUsersEntity.usersEntity).fetchJoin()
                .distinct();


        JPQLQuery<StudyEntity> pageableQuery = getQuerydsl().applyPagination(pageable, query);

        QueryResults<StudyEntity> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }
}
