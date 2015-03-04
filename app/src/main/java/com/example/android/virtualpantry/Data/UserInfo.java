package com.example.android.virtualpantry.Data;

import java.util.List;

/**
 * Created by Garrett on 3/3/2015.
 */
public class UserInfo {

    private static UserInfo userInfo = null;


    public static synchronized UserInfo getUserInfo(){
        return userInfo;
    }

    public static synchronized void updateUserInfo(JSONModels.UserInfoResJSON userInfoJSon){

    }
}
