package com.vend.Constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by makha on 28/06/2018.
 */
@AllArgsConstructor
@Getter
public enum  AuthConst {
    NEW_CODE ( "new_code"),
    USED_CODE ("used_code"),
    OLD_CODE ("old_code"),
    EMAIL_VERIFICATION ("email_verify"),
    FORGOT_PASSWORD_VERIFICATION ("pasword_forget");

    String name;
}
