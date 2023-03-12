package com.runningwith.domain.study;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepositoryExtension {
    Page<StudyEntity> findByKeyword(String keyword, Pageable pageable);
}
