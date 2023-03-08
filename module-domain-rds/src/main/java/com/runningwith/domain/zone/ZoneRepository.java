package com.runningwith.domain.zone;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface ZoneRepository extends JpaRepository<ZoneEntity, Long> {
    Optional<ZoneEntity> findByCityAndProvince(String cityName, String provinceName);
}
