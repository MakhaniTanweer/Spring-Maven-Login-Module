package com.vend.Service;

import com.vend.Entity.User;

/**
 * Created by makha on 17/06/2018.
 */
public interface CommunicationService {
    boolean sendForgotPasswordCode (User user, String code) throws Exception;
    boolean sendVerificationCode(User user, String code) throws Exception;
}
