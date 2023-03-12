package com.runningwith.domain.study;

import com.runningwith.domain.tag.TagEntity;
import com.runningwith.domain.zone.ZoneEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface StudyRepositoryExtension {
    Page<StudyEntity> findByKeyword(String keyword, Pageable pageable);

    List<StudyEntity> findByUsers(Set<TagEntity> tags, Set<ZoneEntity> zones);
}
