package com.runningwith.users;

import com.runningwith.account.AccountEntity;
import com.runningwith.study.StudyEntity;
import com.runningwith.tag.TagEntity;
import com.runningwith.zone.ZoneEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.runningwith.utils.CustomStringUtils.getRandomUUID;

@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "users")
public class UsersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_users", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_account")
    private AccountEntity accountEntity;

    @ManyToMany
    @JoinTable(name = "users_tags", joinColumns = @JoinColumn(name = "id_users"), inverseJoinColumns = @JoinColumn(name = "id_tag"))
    private Set<TagEntity> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "users_zones", joinColumns = @JoinColumn(name = "id_users"), inverseJoinColumns = @JoinColumn(name = "id_zone"))
    private Set<ZoneEntity> zones = new HashSet<>();

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "email_check_token")
    private String emailCheckToken;

    @Column(name = "email_check_token_generated_at")
    private LocalDateTime emailCheckTokenGeneratedAt;

    private String bio;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    private String occupation;

    private String location;

    @Column(name = "study_created_by_email")
    private boolean studyCreatedByEmail = false;

    @Column(name = "study_created_by_web")
    private boolean studyCreatedByWeb = true;

    @Column(name = "study_enrollment_result_by_email")
    private boolean studyEnrollmentResultByEmail = false;

    @Column(name = "study_enrollment_result_by_web")
    private boolean studyEnrollmentResultByWeb = true;

    @Column(name = "study_updated_by_email")
    private boolean studyUpdatedByEmail = false;

    @Column(name = "study_updated_by_web")
    private boolean studyUpdatedByWeb = true;

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "longtext", name = "profile_image")
    private String profileImage;

    public void generateEmailCheckToken() {
        this.emailCheckToken = getRandomUUID();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public boolean isValidEmailToken(String emailCheckToken) {
        return this.emailCheckToken.equals(emailCheckToken);
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void updateProfile(String bio, String profileUrl, String occupation, String location, String profileImage) {
        this.bio = bio;
        this.profileUrl = profileUrl;
        this.occupation = occupation;
        this.location = location;
        this.profileImage = profileImage;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateNotifications(boolean studyCreatedByEmail, boolean studyCreatedByWeb, boolean studyEnrollmentResultByEmail, boolean studyEnrollmentResultByWeb, boolean studyUpdatedByEmail, boolean studyUpdatedByWeb) {
        this.studyCreatedByEmail = studyCreatedByEmail;
        this.studyCreatedByWeb = studyCreatedByWeb;
        this.studyEnrollmentResultByEmail = studyEnrollmentResultByEmail;
        this.studyEnrollmentResultByWeb = studyEnrollmentResultByWeb;
        this.studyUpdatedByEmail = studyUpdatedByEmail;
        this.studyUpdatedByWeb = studyUpdatedByWeb;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isManagerOf(StudyEntity studyEntity) {
        return studyEntity.isManager(this);
    }
}
