package com.vend.Service;

import com.vend.Dto.AuthResponse;
import com.vend.Dto.LoginDto;
import com.vend.Dto.SignUpDto;
import com.vend.Dto.UserDto;


public interface AuthService {
    AuthResponse signup (SignUpDto user) throws Exception;
    AuthResponse login (LoginDto loginDetails) throws Exception;
    boolean logout(String authenticationCode, Integer userId) throws Exception;
    AuthResponse requestPasswordChange (String authenticationToken, String  emailId,  String oldPassword, String newPassword) throws Exception;
    AuthResponse requestForgotPasswordCode (String email, String authenticationToken) throws Exception;
    AuthResponse verifyForgotPasswordCode(LoginDto userDto, String authenticationToken, String passwordCode) throws Exception;
    AuthResponse resendVerificationCode(String email, String authenticationToken, String Type) throws Exception;
    AuthResponse verifyEmail (String email, String authenticationToken, String verificationCode) throws Exception;
}
