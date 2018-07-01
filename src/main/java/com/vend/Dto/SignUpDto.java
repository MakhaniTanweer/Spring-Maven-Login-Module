package com.vend.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by makha on 28/06/2018.
 */
@Data
@NoArgsConstructor
public class SignUpDto {
    String contactNumber;
    String name;
    String email;
    String password;


}
