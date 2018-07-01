package com.vend.Service.helper;

import java.security.SecureRandom;


public class RandomCodeUtil
{
    private static final String SEED                    = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int    CODE_LENGTH      =  6;


    public static String getCode()
    {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++)
                sb.append(SEED.charAt(rnd.nextInt(SEED.length())));

        return sb.toString();

    }

    public static String generateToken(){
        String uid = java.util.UUID.randomUUID().toString();
        return uid.replace("-", "");
    }
}
