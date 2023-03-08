package com.runningwith.domain.users.form;

import com.runningwith.domain.users.UsersEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@EqualsAndHashCode(of = {"bio", "profileUrl", "occupation", "location", "profileImage"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    @Length(max = 35)
    private String bio;

    @Length(max = 50)
    private String profileUrl;

    @Length(max = 50)
    private String occupation;

    @Length(max = 50)
    private String location;

    private String profileImage;

    public static Profile toProfile(UsersEntity usersEntity) {
        return new Profile(usersEntity.getBio(), usersEntity.getProfileUrl(), usersEntity.getOccupation(), usersEntity.getLocation(), usersEntity.getProfileImage());
    }
}
