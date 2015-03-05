package com.example.android.virtualpantry.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Garrett on 3/3/2015.
 */
public class UserInfo {

    private static UserInfo userInfo = null;

    private long userID;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private List<HouseholdBrief> householdBriefs;


    public static synchronized UserInfo getUserInfo(){
        return userInfo;
    }

    public static synchronized void updateUserInfo(JSONModels.UserInfoResJSON userInfoJSON){
        userInfo = new UserInfo(userInfoJSON);
    }

    private UserInfo(JSONModels.UserInfoResJSON userInfoJSON){
        userID = userInfoJSON.userID;
        firstName = userInfoJSON.firstName;
        lastName = userInfoJSON.lastName;
        emailAddress = userInfoJSON.emailAddress;
        householdBriefs = new ArrayList<HouseholdBrief>();
        for(JSONModels.HouseholdShortJSON householdShortJSON : userInfoJSON.households){
            householdBriefs.add(new HouseholdBrief(householdShortJSON));
        }
    }

    //region getters
    public long getUserID() {
        return userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public List<HouseholdBrief> getHouseholdBriefs() {
        return householdBriefs;
    }
    //endregion

}
