package com.runningwith.domain.study;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyRepositoryExtension {
    List<StudyEntity> findByKeyword(String keyword);
}
