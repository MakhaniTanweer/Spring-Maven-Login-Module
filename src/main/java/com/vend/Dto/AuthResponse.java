package com.vend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by makha on 17/06/2018.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    UserDto user;
    String authenticationToken;
}
