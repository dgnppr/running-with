package com.runningwith.domain.study;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import com.runningwith.domain.tag.QTagEntity;
import com.runningwith.domain.tag.TagEntity;
import com.runningwith.domain.users.QUsersEntity;
import com.runningwith.domain.zone.QZoneEntity;
import com.runningwith.domain.zone.ZoneEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

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

    @Override
    public List<StudyEntity> findByUsers(Set<TagEntity> tags, Set<ZoneEntity> zones) {
        QStudyEntity studyEntity = QStudyEntity.studyEntity;

        JPQLQuery<StudyEntity> query = from(studyEntity).where(studyEntity.published.isTrue()
                        .and(studyEntity.closed.isFalse())
                        .and(studyEntity.tags.any().in(tags))
                        .and(studyEntity.zones.any().in(zones)))
                .leftJoin(studyEntity.tags, QTagEntity.tagEntity).fetchJoin()
                .leftJoin(studyEntity.zones, QZoneEntity.zoneEntity).fetchJoin()
                .orderBy(studyEntity.publishedDatetime.desc())
                .distinct()
                .limit(9);

        return query.fetch();
    }
}
