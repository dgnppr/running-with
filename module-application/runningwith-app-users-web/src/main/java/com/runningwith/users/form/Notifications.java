package com.runningwith.users.form;

import com.runningwith.users.UsersEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
