package com.runningwith.domain.study;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<StudyEntity, Long>, StudyRepositoryExtension {
    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"tags", "zones", "managers", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<StudyEntity> findByPath(String path);

    @EntityGraph(attributePaths = {"tags", "managers"})
    Optional<StudyEntity> findStudyEntityWithTagsByPath(String path);

    @EntityGraph(attributePaths = {"zones", "managers"})
    Optional<StudyEntity> findStudyEntityWithZonesByPath(String path);

    @EntityGraph(attributePaths = {"managers"})
    Optional<StudyEntity> findStudyEntityWithManagersByPath(String path);

    @EntityGraph(attributePaths = {"members"})
    Optional<StudyEntity> findStudyEntityWithMembersByPath(String path);

    Optional<StudyEntity> findStudyEntityOnlyByPath(String path);

    @EntityGraph(attributePaths = {"tags", "zones"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<StudyEntity> findStudyEntityWithTagsAndZonesById(Long id);

    @EntityGraph(attributePaths = {"managers", "members"})
    Optional<StudyEntity> findStudyEntityWithManagersAndMembersById(Long id);
}
