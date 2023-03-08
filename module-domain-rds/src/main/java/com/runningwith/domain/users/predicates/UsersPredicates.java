package com.runningwith.domain.users.predicates;

import com.querydsl.core.types.Predicate;
import com.runningwith.domain.tag.TagEntity;
import com.runningwith.domain.users.QUsersEntity;
import com.runningwith.domain.zone.ZoneEntity;

import java.util.Set;

public class UsersPredicates {

    public static Predicate findByTagsAndZones(Set<TagEntity> tags, Set<ZoneEntity> zones) {
        QUsersEntity usersEntity = QUsersEntity.usersEntity;
        return usersEntity.zones.any().in(zones).and(usersEntity.tags.any().in(tags));
    }
}
