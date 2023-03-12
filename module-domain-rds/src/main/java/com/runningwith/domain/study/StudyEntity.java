package com.runningwith.domain.study;

import com.runningwith.domain.tag.TagEntity;
import com.runningwith.domain.users.UsersEntity;
import com.runningwith.domain.zone.ZoneEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "study")
public class StudyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_study", nullable = false)
    private Long id;

    @ManyToMany
    @JoinTable(name = "study_managers", joinColumns = @JoinColumn(name = "id_study"), inverseJoinColumns = @JoinColumn(name = "id_users"))
    private Set<UsersEntity> managers;

    @ManyToMany
    @JoinTable(name = "study_members", joinColumns = @JoinColumn(name = "id_study"), inverseJoinColumns = @JoinColumn(name = "id_users"))
    private Set<UsersEntity> members;

    @ManyToMany
    @JoinTable(name = "study_tags", joinColumns = @JoinColumn(name = "id_study"), inverseJoinColumns = @JoinColumn(name = "id_tag"))
    private Set<TagEntity> tags;

    @ManyToMany
    @JoinTable(name = "study_zones", joinColumns = @JoinColumn(name = "id_study"), inverseJoinColumns = @JoinColumn(name = "id_zone"))
    private Set<ZoneEntity> zones;

    @Column(unique = true, nullable = false)
    private String path;

    @Column(nullable = false)
    private String title;

    @Column(name = "short_description", nullable = false)
    private String shortDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "full_description", nullable = false)
    private String fullDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "banner_image")
    private String bannerImage;

    @Column(name = "published_datetime")
    private LocalDateTime publishedDatetime;

    @Column(name = "closed_datetime")
    private LocalDateTime closedDateTime;

    @Column(name = "recruiting_updated_datetime")
    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting = true;

    private boolean published = false;

    private boolean closed = false;

    @Column(name = "use_banner")
    private boolean useBanner = false;

    @Column(name = "member_count")
    private int memberCount = 0;

    public void addManager(UsersEntity usersEntity) {
        this.managers.add(usersEntity);
    }

    public boolean isJoinable(UsersEntity usersEntity) {
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(usersEntity) && !this.managers.contains(usersEntity);

    }

    public boolean isMember(UsersEntity usersEntity) {
        return this.members.contains(usersEntity);
    }

    public boolean isManager(UsersEntity usersEntity) {
        return this.managers.contains(usersEntity);
    }

    public void updateDescription(String shortDescription, String fullDescription) {
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
    }

    public void updateUseBanner(boolean useBanner) {
        this.useBanner = useBanner;
    }

    public String getBannerImage() {
        return bannerImage != null ? bannerImage : "/images/default_banner.png";
    }

    public void updateStudyBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }

    public void publish() {
        if (!this.closed && !this.published) {
            this.published = true;
            this.publishedDatetime = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
        }
    }

    public void close() {
        if (this.published && !this.closed) {
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
        }
    }

    public boolean isRecruitUpdatable() {
        return (this.published) && (this.recruitingUpdatedDateTime == null || this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1)));
    }

    public void startRecruit() {
        if (isRecruitUpdatable()) {
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public void stopRecruit() {
        if (isRecruitUpdatable()) {
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("인원 모집을 멈출 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public void updatePath(String newPath) {
        this.path = newPath;
    }

    public void updateTitle(String newTitle) {
        this.title = newTitle;
    }

    public boolean isRemovable() {
        return !this.published;
    }

    public void addMember(UsersEntity usersEntity) {
        this.getMembers().add(usersEntity);
        this.memberCount += 1;
    }

    public void removeMember(UsersEntity usersEntity) {
        this.getMembers().remove(usersEntity);
        this.memberCount -= 1;
    }
}
