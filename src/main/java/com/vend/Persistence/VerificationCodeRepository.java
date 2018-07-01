package com.vend.Persistence;

import com.vend.Entity.VerificationCode;

import java.util.List;

public interface VerificationCodeRepository extends BaseRepository<VerificationCode> {
    List<VerificationCode> findByUserIdAndType (Integer userId, String type);
    VerificationCode findByUserIdAndCodeAndType(Integer userId, String code, String type);
}
