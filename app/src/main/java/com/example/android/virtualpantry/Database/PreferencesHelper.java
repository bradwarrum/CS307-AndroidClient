package com.example.android.virtualpantry.Database;

import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by Garrett on 4/6/2015.
 */
public class PreferencesHelper {

    public static final String USER_INFO = "UserInfoPrefs";

    //login info
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String TOKEN = "token";

    public static final String NULL_PREFERENCE_VALUE = "null_preference";

    public static boolean isNull(String value){
        if(TextUtils.isEmpty(value) | value.equals(NULL_PREFERENCE_VALUE)){
            return true;
        }
        return false;
    }



}
