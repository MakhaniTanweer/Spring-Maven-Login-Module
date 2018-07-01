package com.vend.Service.impl;

import com.vend.Constant.AuthConst;
import com.vend.Constant.Error;
import com.vend.Dto.AuthResponse;
import com.vend.Dto.LoginDto;
import com.vend.Dto.SignUpDto;
import com.vend.Dto.UserDto;
import com.vend.Entity.AuthenticationToken;
import com.vend.Entity.User;
import com.vend.Entity.VerificationCode;
import com.vend.Persistence.AuthenticationTokenRepository;
import com.vend.Persistence.UserRepository;
import com.vend.Persistence.VerificationCodeRepository;
import com.vend.Service.AuthService;
import com.vend.Service.CommunicationService;
import com.vend.Service.helper.EntityHelper;
import com.vend.Service.helper.RandomCodeUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by makha on 11/06/2018.
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    VerificationCodeRepository verificationCodeRepository;

    @Autowired
    AuthenticationTokenRepository authenticationTokenRepository;

    @Autowired
    @Qualifier("EmailService")
    CommunicationService communicationService;



    @Override
    public AuthResponse signup(SignUpDto signUpDto) throws Exception {
        if (EntityHelper.isValidPassword(signUpDto.getPassword()))
            throw new Exception(Error.INVALID_PASSWORD_FORMAT.getMessage());

        if (EntityHelper.isValidEmailAddress(signUpDto.getEmail()))
            throw new Exception(Error.INVALID_EMAIL_FORMAT.getMessage());

        if (EntityHelper.isValidPhoneNumber(signUpDto.getContactNumber()))
            throw new Exception(Error.INVALID_PHONR_NUMBER_FORMAT.getMessage());

        List<User> users = userRepository.findAll();
        if(users.stream().anyMatch(user -> user.getEmail().equals(signUpDto.getEmail())))
            throw new Exception(Error.NON_UNIQUE_EMAIL_ID.getMessage());

        if(users.stream().anyMatch(user -> user.getContactNumber().equals(signUpDto.getContactNumber())))
            throw new Exception(Error.NON_UNIQUE_PHONE_NUMBER.getMessage());

        User user = registerUser(signUpDto);
        if (EntityHelper.isNull(user) || EntityHelper.isNull(user.getId()))
            throw new Exception(Error.DATABASE_SAVE_FAILED.getMessage());

        VerificationCode verificationCode = generateCode(user, AuthConst.NEW_CODE.getName(), AuthConst.EMAIL_VERIFICATION.getName());
        boolean emailSent = communicationService.sendVerificationCode(user,verificationCode.getCode());
        if (! emailSent)
            throw new Exception(Error.FAILED_COMMUNICATION_TO_USER.getMessage());

        verificationCodeRepository.save(verificationCode);
        AuthenticationToken authenticationToken = createAuthenticatedTokenForUser(user);
        if (EntityHelper.isNull(authenticationToken))
            throw new Exception(Error.DATABASE_SAVE_FAILED.getMessage());

        AuthResponse authResponse = new AuthResponse(new UserDto(user), authenticationToken.getToken());
        return authResponse;
    }

    @Override
    public AuthResponse login(LoginDto userDto) throws Exception {
        if (!EntityHelper.isStringSet(userDto.getEmail()) || !EntityHelper.isStringSet(userDto.getPassword()))
            throw new Exception(Error.REQUIRED_FIELDS_MISSING.getMessage());

        boolean validLogin = checkVerifiedLogin(userDto);

        if (! validLogin)
            throw new Exception(Error.INVALID_EMAIL_PASSWORD_COMBINATION.getMessage());

        User user = userRepository.findUserByEmail(userDto.getEmail());

        if (EntityHelper.isNull(user))
            throw new Exception(Error.DATABSE_RETRIEVAL_FAILED.getMessage());

        authenticationTokenRepository.deleteByUserId(user.getId());
            AuthenticationToken authenticationToken = createAuthenticatedTokenForUser(user);
            return new AuthResponse(new UserDto(user), authenticationToken.getToken());
    }

    @Override
    public boolean logout(String authenticationToken, Integer userId) throws Exception {
        if (EntityHelper.allNull(authenticationToken,userId))
            throw new Exception(Error.REQUIRED_FIELDS_MISSING.getMessage());

        checkValidLogOut(authenticationToken,userId);
        return true;
    }

    @Override
    public AuthResponse requestPasswordChange(String authenticationToken, String  emailId,  String oldPassword, String newPassword) throws  Exception {
        if (EntityHelper.anyNull(authenticationToken, emailId, oldPassword, newPassword) || !EntityHelper.isValidEmailAddress(emailId))
            throw new Exception(Error.REQUIRED_FIELDS_MISSING.getMessage());

        AuthResponse authResponse = changePassword(authenticationToken, emailId,  oldPassword, newPassword);
        return authResponse;
    }

    @Override
    public AuthResponse requestForgotPasswordCode(String email, String authenticationToken) throws Exception {
        if(EntityHelper.anyNull(email,authenticationToken))
            throw new Exception(Error.REQUIRED_FIELDS_MISSING.getMessage());

        createAndSendForgotPasswordCode(email,authenticationToken);
        User user = userRepository.findUserByEmail(email);
        AuthenticationToken authToken = authenticationTokenRepository.findByUserId(user.getId());
        return new AuthResponse(new UserDto(user),authToken.getToken());
    }

    @Override
    public AuthResponse verifyForgotPasswordCode(LoginDto userDto, String authenticationToken, String passwordCode) throws Exception {
        if (EntityHelper.anyNull(userDto, authenticationToken, passwordCode))
            throw new Exception(Error.REQUIRED_FIELDS_MISSING.getMessage());
        changePasswordUsingForgotPasswordCode (userDto, authenticationToken, passwordCode);
        User user = userRepository.findUserByEmail(userDto.getEmail());
        AuthenticationToken authToken = authenticationTokenRepository.findByUserId(user.getId());
        return new AuthResponse(new UserDto(user),authToken.getToken());
    }

    @Override
    public AuthResponse resendVerificationCode(String email, String authenticationToken, String type) throws Exception {
        if (EntityHelper.anyNull(email, authenticationToken))
            throw new Exception(Error.REQUIRED_FIELDS_MISSING.getMessage());
        reSendACode(email,authenticationToken,type);
        User user = userRepository.findUserByEmail(email);
        AuthenticationToken authToken = authenticationTokenRepository.findByUserId(user.getId());
        return new AuthResponse(new UserDto(user),authToken.getToken());
    }

    @Override
    public AuthResponse verifyEmail(String email, String authenticationToken, String verificationCode) throws Exception {
        if (EntityHelper.anyNull(email,authenticationToken,verificationCode))
            throw new Exception(Error.REQUIRED_FIELDS_MISSING.getMessage());

        verifyEmailCode (email, authenticationToken, verificationCode);
        User user = userRepository.findUserByEmail(email);
        AuthenticationToken authToken = authenticationTokenRepository.findByUserId(user.getId());
        return new AuthResponse(new UserDto(user),authToken.getToken());
    }


    private void verifyEmailCode (String email,String authenticationToken,String signUpCode) throws Exception {
        AuthenticationToken authToken = authenticationTokenRepository.findByToken(authenticationToken);
        if (EntityHelper.isNull(authToken))
            throw new Exception(Error.INVALID_AUTHENTICATION_TOKEN.getMessage());

        if (!authToken.getUser().getEmail().equals(email))
            throw new Exception(Error.ATTEMPT_TO_PERFORM_ACTION_ON_DIFFERENT_EMAIL.getMessage());

        User user = authToken.getUser();
        VerificationCode verificationCode = verificationCodeRepository.findByUserIdAndCodeAndType(user.getId(),signUpCode,AuthConst.EMAIL_VERIFICATION.getName());
        if (EntityHelper.isNull(verificationCode) || verificationCode.getStatus().equals(AuthConst.OLD_CODE.getName()) || verificationCode.getStatus().equals(AuthConst.USED_CODE.getName()))
            throw new Exception(Error.INVALID_VERIFICATION_CODE.getMessage().concat("AND").concat(Error.INVALID_VERIFICATION_TOKEN_TYPE.getMessage()));

        user.setVerifiedUser(true);
        userRepository.save(user);
        verificationCode.setStatus(AuthConst.USED_CODE.getName());
        verificationCodeRepository.save(verificationCode);
    }

    private void changePasswordUsingForgotPasswordCode (LoginDto userDto,String authenticationToken,String passwordCode) throws Exception {
        AuthenticationToken authToken = authenticationTokenRepository.findByToken(authenticationToken);
        if (EntityHelper.isNull(authToken))
            throw new Exception(Error.INVALID_AUTHENTICATION_TOKEN.getMessage());

        User user = authToken.getUser();
        if (EntityHelper.isNull(user))
            throw new Exception(Error.INVALID_AUTHENTICATION_TOKEN.getMessage());

        if (userDto.getEmail().equals(user.getEmail()))
            throw new Exception(Error.ATTEMPT_TO_CHANGE_PASSWORD_FOR_WRONG_EMAIL.getMessage());

        VerificationCode verificationCode = verificationCodeRepository.findByUserIdAndCodeAndType(user.getId(),passwordCode,AuthConst.FORGOT_PASSWORD_VERIFICATION.getName());
        if (EntityHelper.isNull(verificationCode) || verificationCode.getStatus().equals(AuthConst.OLD_CODE.getName()) || verificationCode.getStatus().equals(AuthConst.USED_CODE.getName()))
            throw new Exception(Error.INVALID_VERIFICATION_CODE.getMessage());

        String password = "";
        password = BCrypt.hashpw(userDto.getPassword(), BCrypt.gensalt(12));
        user.setPassword(password);
        userRepository.save(user);

        verificationCode.setStatus(AuthConst.USED_CODE.getName());
        verificationCodeRepository.save(verificationCode);
    }

    private boolean reSendACode(String email, String authenticationToken, String type) throws Exception {
        AuthenticationToken authToken = authenticationTokenRepository.findByToken(authenticationToken);
        if (EntityHelper.isNull(authToken))
            throw new Exception(Error.INVALID_AUTHENTICATION_TOKEN.getMessage());

        if (!authToken.getUser().getEmail().equals(email))
            throw new Exception(Error.ATTEMPT_TO_PERFORM_ACTION_ON_DIFFERENT_EMAIL.getMessage());

        if (!authToken.getUser().getVerifiedUser())
            throw new Exception(Error.MISSING_USER.getMessage());

        if (!(type.equals(AuthConst.EMAIL_VERIFICATION.getName()) || type.equals(AuthConst.FORGOT_PASSWORD_VERIFICATION.getName())))
            throw new Exception(Error.INVALID_VERIFICATION_TOKEN_TYPE.getMessage());

        User user = authToken.getUser();
        List<VerificationCode> verificationCodes = verificationCodeRepository.findByUserIdAndType(user.getId(),type);
        verificationCodes.stream().filter(codes -> codes.getStatus().equals(AuthConst.NEW_CODE.getName())).forEach(code -> {
            code.setStatus(AuthConst.OLD_CODE.getName());
            verificationCodeRepository.save(code);
        });

        VerificationCode verificationCode = generateCode(user,AuthConst.NEW_CODE.getName(),type);
        boolean emailSent = communicationService.sendVerificationCode(user, verificationCode.getCode());
        verificationCodeRepository.save(verificationCode);

        return true;
    }
    private void createAndSendForgotPasswordCode (String email, String authenticationToken) throws Exception {
        User user = userRepository.findUserByEmail(email);
        if(EntityHelper.isNull(user) || ! user.getVerifiedUser())
            throw new Exception(Error.MISSING_USER.getMessage());

        AuthenticationToken authToken = authenticationTokenRepository.findByToken(authenticationToken);
        if (EntityHelper.isNull(authToken) || !authenticationToken.equals(authToken))
            throw new Exception(Error.INVALID_AUTHENTICATION_TOKEN.getMessage());

        VerificationCode verificationCode = generateCode(user, AuthConst.NEW_CODE.getName(), AuthConst.FORGOT_PASSWORD_VERIFICATION.getName());
        boolean codeSent = communicationService.sendForgotPasswordCode(user,verificationCode.getCode());

        if(! codeSent)
            throw new Exception(Error.FAILED_COMMUNICATION_TO_USER.getMessage());

        verificationCodeRepository.save(verificationCode);
    }
    private AuthResponse changePassword (String authenticationToken, String email, String oldPassword, String newPassword) throws Exception {
        AuthenticationToken authToken = authenticationTokenRepository.findByToken(authenticationToken);
        if (EntityHelper.isNull(authToken))
            throw new Exception(Error.INVALID_AUTHENTICATION_TOKEN.getMessage());

        if (!authToken.getUser().getEmail().equals(email) )
            throw new Exception(Error.ATTEMPT_TO_CHANGE_PASSWORD_FOR_WRONG_EMAIL.getMessage());

        if (!authToken.getUser().getVerifiedUser())
            throw new Exception(Error.MISSING_USER.getMessage());

        if (!BCrypt.checkpw(oldPassword,authToken.getUser().getPassword()))
            throw new Exception(Error.OLD_PASSWORD_MISMATCH.getMessage());

        User user = authToken.getUser();
        authenticationTokenRepository.deleteByUserId(user.getId());
        String password = "";
        password = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        user.setPassword(password);
        user = userRepository.save(user);
        AuthenticationToken newAuthToken = createAuthenticatedTokenForUser(user);

        return new AuthResponse(new UserDto(user), newAuthToken.getToken());

    }
    private AuthenticationToken createAuthenticatedTokenForUser (User user) {
        AuthenticationToken authenticationToken = new AuthenticationToken();
        authenticationToken.setUser(user);
        authenticationToken.setToken(RandomCodeUtil.generateToken());

        return authenticationTokenRepository.save(authenticationToken);
    }
    private VerificationCode generateCode(User user, String status, String type) {
        String code = RandomCodeUtil.getCode();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode(code);
        verificationCode.setUser(user);
        verificationCode.setStatus(AuthConst.NEW_CODE.getName());
        verificationCode.setType(type);
        verificationCode.setExpiry(new Timestamp(System.currentTimeMillis() + (1000 * 60 * 10)));
        verificationCode.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));

        return verificationCode;
    }
    private User registerUser (SignUpDto signUpDto) {
        try {
            User user  = new User(signUpDto);
            return userRepository.save(user);
        } catch (Exception e) {
            return null;
        }
    }
    private boolean checkVerifiedLogin (LoginDto userDto) {
        User user = userRepository.findUserByEmail(userDto.getEmail());
        return BCrypt.checkpw(userDto.getPassword(),user.getPassword());
    }
    private boolean checkValidLogOut (String authenticationToken, Integer userId) throws Exception {
        User user = userRepository.findById(userId).orElse(null);
        if (EntityHelper.isNull(user) || !user.getVerifiedUser())
            throw new Exception(Error.MISSING_USER.getMessage());

        AuthenticationToken authToken = authenticationTokenRepository.findByUserId(userId);
        if (EntityHelper.isNull(authToken) || !authenticationToken.equals(authToken))
            throw new Exception(Error.INVALID_AUTHENTICATION_TOKEN.getMessage());

        authenticationTokenRepository.deleteByUserId(user.getId());
        return true;

    }
}
