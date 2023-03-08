package com.runningwith.domain.tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;


    public TagEntity findOrCreateNew(String title) {
        Optional<TagEntity> optionalTagEntity = tagRepository.findByTitle(title);

        if (optionalTagEntity.isEmpty()) {
            TagEntity tagEntity = TagEntity.builder()
                    .title(title)
                    .build();

            return tagRepository.save(tagEntity);
        }

        return optionalTagEntity.get();
    }
}
