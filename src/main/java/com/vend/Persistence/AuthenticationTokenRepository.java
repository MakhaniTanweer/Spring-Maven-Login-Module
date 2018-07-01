package com.vend.Persistence;

import com.vend.Entity.AuthenticationToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;


public interface AuthenticationTokenRepository extends BaseRepository<AuthenticationToken> {
    AuthenticationToken findByToken(String token);
    AuthenticationToken findByUserId(Integer userId);

    //    @Query("SELECT a FROM AuthenticationToken a where a.user.id = :userId and a.token = :token and ")
    AuthenticationToken findByUserIdAndToken(Integer userId, String token);

    @Transactional
    @Modifying
    void deleteByUserId(Integer userId);
}
