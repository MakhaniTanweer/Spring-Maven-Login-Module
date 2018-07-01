package com.vend.Constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by makha on 01/07/2018.
 */
@AllArgsConstructor
@Getter
public enum Error {
    INVALID_PASSWORD_FORMAT ("The password fails to conform with the standard password format"),
    INVALID_EMAIL_FORMAT ("The email format is invalid"),
    INVALID_PHONR_NUMBER_FORMAT ("The phone number format is invalid"),
    NON_UNIQUE_EMAIL_ID ("The email address is already registered by another user"),
    NON_UNIQUE_PHONE_NUMBER ("The phone number is already registered by another user"),
    FAILED_COMMUNICATION_TO_USER ("The email/ message was not sent to the user"),
    DATABASE_SAVE_FAILED ("Failed to save record in database"),
    DATABSE_RETRIEVAL_FAILED ("Failed to retrieve record from databse"),
    REQUIRED_FIELDS_MISSING("The required fields are missing"),
    MISSING_USER ("The user is unverified or deosn't exist"),
    INVALID_AUTHENTICATION_TOKEN ("The authentication token is invalid"),
    ATTEMPT_TO_CHANGE_PASSWORD_FOR_WRONG_EMAIL ("Cannot change password for a different email than the user's own"),
    OLD_PASSWORD_MISMATCH ("The old password for password reset is incorrect"),
    INVALID_EMAIL_PASSWORD_COMBINATION ("The email or password is incorrect"),
    INVALID_VERIFICATION_CODE ("The verification code is incorrect, expired or already used"),
    ATTEMPT_TO_PERFORM_ACTION_ON_DIFFERENT_EMAIL("Cannot perform action on an email other than the user's won"),
    INVALID_VERIFICATION_TOKEN_TYPE ("The verification token type is invalid");

    private String message;

}
