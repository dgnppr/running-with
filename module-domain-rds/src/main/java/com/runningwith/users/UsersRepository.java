package com.runningwith.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface UsersRepository extends JpaRepository<UsersEntity,Long> {

    UsersEntity findByEmail(String email);
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
