package com.runningwith.domain.users;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface UsersRepository extends JpaRepository<UsersEntity, Long>, QuerydslPredicateExecutor<UsersEntity> {
    Optional<UsersEntity> findByEmail(String email);

    Optional<UsersEntity> findByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    @EntityGraph(attributePaths = {"tags", "zones"})
    UsersEntity findUsersEntityWithTagsAndZonesById(Long id);
}
