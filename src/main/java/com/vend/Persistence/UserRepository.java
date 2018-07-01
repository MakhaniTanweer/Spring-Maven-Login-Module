package com.vend.Persistence;

import com.vend.Entity.User;

/**
 * Created by makha on 28/06/2018.
 */
public interface UserRepository extends BaseRepository<User> {
    User findUserByEmail(String username);
}
