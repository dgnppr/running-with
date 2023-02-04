package com.runningwith.users;

import com.runningwith.account.AccountEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(of = "id")
@AllArgsConstructor @Builder
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

    private String email;

    private String password;

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

    private String occupation;

    private String location;

    @Column(name = "study_created_by_email")
    private boolean studyCreatedByEmail;

    @Column(name = "study_created_by_web")
    private boolean studyCreatedByWeb = true;

    @Column(name = "study_enrollment_result_by_email")
    private boolean studyEnrollmentResultByEmail;

    @Column(name = "study_enrollment_result_by_web")
    private boolean studyEnrollmentResultByWeb = true;

    @Column(name = "study_updated_by_email")
    private boolean studyUpdatedByEmail;

    @Column(name = "study_updated_by_web")
    private boolean studyUpdatedByWeb = true;

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "longtext",name = "profile_image")
    private String profileImage;

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }
}
