package com.runningwith.domain.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface TagRepository extends JpaRepository<TagEntity, Long> {
    Optional<TagEntity> findByTitle(String title);
}
