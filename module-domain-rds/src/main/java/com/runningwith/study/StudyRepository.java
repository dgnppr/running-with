package com.runningwith.study;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<StudyEntity, Long> {
    boolean existsByPath(String path);
}
