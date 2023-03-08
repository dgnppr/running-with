package com.runningwith.domain.event;

import com.runningwith.domain.users.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {
    boolean existsByEventEntityAndUsersEntity(EventEntity eventEntity, UsersEntity usersEntity);

    Optional<EnrollmentEntity> findByEventEntityAndUsersEntity(EventEntity eventEntity, UsersEntity usersEntity);
}
