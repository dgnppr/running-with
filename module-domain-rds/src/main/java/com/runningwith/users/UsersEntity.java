package com.runningwith.users;

import com.runningwith.account.AccountEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UsersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_users", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_account")
    private AccountEntity accountEntity;

    private String email;

    private String password;

    private String nickname;

    private boolean emailVerified;

    private String emailCheckToken;

    private String bio;

    private String profileUrl;

    private String occupation;

    private String location;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb = true;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb = true;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;

    private String profileImage;
}
