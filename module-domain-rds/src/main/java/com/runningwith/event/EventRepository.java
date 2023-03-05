package com.runningwith.event;

import com.runningwith.study.StudyEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<EventEntity, Long> {

    @EntityGraph(attributePaths = {"enrollments"}, type = EntityGraph.EntityGraphType.LOAD)
    List<EventEntity> findByStudyEntityOrderByStartDateTime(StudyEntity studyEntity);
}
