create table runningwith.account
(
    id_account bigint auto_increment
        primary key,
    type       char(20) not null
);

create table runningwith.persistent_logins
(
    username  varchar(64) not null,
    series    varchar(64) not null
        primary key,
    token     varchar(64) not null,
    last_used timestamp   not null
);

create table runningwith.study
(
    id_study                    bigint auto_increment
        primary key,
    title                       varchar(50)  not null,
    short_description           varchar(100) not null,
    full_description            text         not null,
    path                        varchar(50)  not null,
    published_datetime          datetime     null,
    closed_datetime             datetime     null,
    banner_image                longtext     null,
    published                   bit          not null,
    recruiting_updated_datetime datetime     null,
    recruiting                  bit          null,
    closed                      bit          null,
    use_banner                  bit          null,
    member_count                int          not null,
    constraint UQ_study_path
        unique (path)
);

create table runningwith.tag
(
    id_tag bigint auto_increment
        primary key,
    title  varchar(45) not null,
    constraint tag_pk
        unique (title)
);

create table runningwith.study_tags
(
    id_study bigint not null,
    id_tag   bigint not null,
    constraint FK_study_study_tags
        foreign key (id_study) references runningwith.study (id_study),
    constraint FK_tag_study_tags
        foreign key (id_tag) references runningwith.tag (id_tag)
);

create table runningwith.users
(
    id_users                         bigint auto_increment
        primary key,
    id_account                       bigint       not null,
    email                            varchar(50)  not null,
    password                         varchar(200) not null,
    nickname                         varchar(50)  not null,
    email_verified                   bit          not null,
    email_check_token                varchar(50)  not null,
    email_check_token_generated_at   datetime     not null,
    bio                              varchar(50)  null,
    profile_url                      varchar(100) null,
    occupation                       varchar(50)  null,
    location                         varchar(50)  null,
    study_created_by_web             bit          not null,
    study_created_by_email           bit          not null,
    study_enrollment_result_by_web   bit          not null,
    study_enrollment_result_by_email bit          not null,
    study_updated_by_email           bit          not null,
    study_updated_by_web             bit          not null,
    profile_image                    longtext     null,
    joined_at                        datetime     null,
    constraint users_email
        unique (email),
    constraint users_nickname
        unique (nickname),
    constraint FK_account_users
        foreign key (id_account) references runningwith.account (id_account)
);

create table runningwith.event
(
    id_event                 bigint auto_increment
        primary key,
    title                    varchar(50) not null,
    description              text        not null,
    limit_of_enrollments     int         not null,
    event_type               varchar(50) not null,
    id_study                 bigint      not null,
    id_users                 bigint      not null,
    created_date_time        datetime    not null,
    end_enrollment_date_time datetime    not null,
    start_date_time          datetime    null,
    end_date_time            datetime    null,
    constraint FK_study_event
        foreign key (id_study) references runningwith.study (id_study),
    constraint FK_users_event
        foreign key (id_users) references runningwith.users (id_users)
);

create table runningwith.enrollment
(
    id_enrollment bigint auto_increment
        primary key,
    accepted      bit      not null,
    attended      bit      not null,
    enrolled_at   datetime not null,
    id_users      bigint   not null,
    id_event      bigint   not null,
    constraint FK_event_enrollment
        foreign key (id_event) references runningwith.event (id_event),
    constraint FK_users_enrollment
        foreign key (id_users) references runningwith.users (id_users)
);

create table runningwith.notification
(
    id_notification   bigint auto_increment
        primary key,
    created_time      datetime     null,
    title             varchar(45)  null,
    link              varchar(200) null,
    message           varchar(200) null,
    checked           bit          null,
    notification_type varchar(45)  not null,
    id_users          bigint       not null,
    constraint FK_users_notification
        foreign key (id_users) references runningwith.users (id_users)
);

create table runningwith.study_managers
(
    id_study bigint not null,
    id_users bigint not null,
    constraint FK_managers_study_managers
        foreign key (id_users) references runningwith.users (id_users),
    constraint FK_study_study_managers
        foreign key (id_study) references runningwith.study (id_study)
);

create table runningwith.study_members
(
    id_study bigint null,
    id_users bigint null,
    constraint FK_members_study_members
        foreign key (id_users) references runningwith.users (id_users),
    constraint FK_study_study_members
        foreign key (id_study) references runningwith.study (id_study)
);

create table runningwith.users_tags
(
    id_tag   bigint not null,
    id_users bigint not null,
    constraint FK_tag_users_tags
        foreign key (id_tag) references runningwith.tag (id_tag),
    constraint FK_users_users_tags
        foreign key (id_users) references runningwith.users (id_users)
);

create table runningwith.zone
(
    id_zone            bigint auto_increment
        primary key,
    city               varchar(45) not null,
    local_name_of_city varchar(45) not null,
    province           varchar(45) null
);

create table runningwith.study_zones
(
    id_study bigint not null,
    id_zone  bigint not null,
    constraint FK_study_study_zones
        foreign key (id_study) references runningwith.study (id_study),
    constraint FK_zone_sutdy_zones
        foreign key (id_zone) references runningwith.zone (id_zone)
);

create table runningwith.users_zones
(
    id_users bigint not null,
    id_zone  bigint null,
    constraint FK_users_users_zones
        foreign key (id_users) references runningwith.users (id_users),
    constraint FK_zone_users_zones
        foreign key (id_zone) references runningwith.zone (id_zone)
);

