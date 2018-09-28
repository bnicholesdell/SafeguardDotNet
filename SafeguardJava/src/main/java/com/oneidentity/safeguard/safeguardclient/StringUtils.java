package com.oneidentity.safeguard.safeguardclient;

public class StringUtils {

    private StringUtils() {
    }
    
    public static boolean isNullOrEmpty(String param) {
        return param == null || param.trim().length() == 0;
    }
    
}
