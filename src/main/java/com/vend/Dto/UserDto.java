package com.vend.Dto;

import com.vend.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by makha on 26/05/2018.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    Integer id;
    String name;
    String email;

    public UserDto (User user) {
        id = user.getId();
        name = user.getName();
        email = user.getEmail();
    }
}
