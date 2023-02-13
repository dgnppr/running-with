package com.runningwith.users.form;

import com.runningwith.users.UsersEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(of = {"studyCreatedByEmail", "studyCreatedByWeb", "studyEnrollmentResultByEmail", "studyEnrollmentResultByWeb", "studyUpdatedByEmail", "studyUpdatedByWeb"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notifications {

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;

    public Notifications(UsersEntity usersEntity) {
        this.studyCreatedByEmail = usersEntity.isStudyCreatedByEmail();
        this.studyCreatedByWeb = usersEntity.isStudyCreatedByWeb();
        this.studyEnrollmentResultByEmail = usersEntity.isStudyEnrollmentResultByEmail();
        this.studyEnrollmentResultByWeb = usersEntity.isStudyUpdatedByWeb();
        this.studyUpdatedByEmail = usersEntity.isStudyUpdatedByEmail();
        this.studyUpdatedByWeb = usersEntity.isStudyUpdatedByWeb();
    }
}
