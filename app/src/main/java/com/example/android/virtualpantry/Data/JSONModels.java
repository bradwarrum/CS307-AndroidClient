package com.example.android.virtualpantry.Data;

import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

/**
 * Created by Garrett on 4/6/2015.
 */
public class JSONModels {
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static class JSONModel {
        public String toString(){
            return gson.toJson(this);
        }

    }

    public static class LoginReqJSON extends JSONModel{
        private final String emailAddress;
        private final String password;

        public LoginReqJSON(String emailAddress, String password) {
            this.emailAddress = emailAddress;
            this.password = password;
        }
    }

    public static class LoginResJSON{
        public final String firstName;
        public final String lastName;
        public final int userID;
        public final String token;

        LoginResJSON(String firstName, String lastName, int userID, String token){
            this.firstName = firstName;
            this.lastName = lastName;
            this.userID = userID;
            this.token = token;
        }
    }

    public static class RegisterReqJSON extends JSONModel{
        private final String emailAddress;
        private final String password;
        private final String firstName;
        private final String lastName;

        public RegisterReqJSON(String emailAddress, String password, String firstName, String lastName) {
            this.emailAddress = emailAddress;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    public static class UserInfoResJSON {
        public final long userID;
        public final String firstName;
        public final String lastName;
        public final String emailAddress;
        public final List<HouseholdShortJSON> households;

        public UserInfoResJSON(
                long userID,
                String firstName,
                String lastName,
                String emailAddress,
                List<HouseholdShortJSON> households) {
            this.userID = userID;
            this.firstName = firstName;
            this.lastName = lastName;
            this.emailAddress = emailAddress;
            this.households = households;
        }
    }

    public static class HouseholdShortJSON{
        public final long householdID;
        public final String householdName;
        public final String householdDescription;

        public HouseholdShortJSON(long householdID, String householdName, String householdDescription) {
            this.householdID = householdID;
            this.householdName = householdName;
            this.householdDescription = householdDescription;
        }
    }

    public static class HouseholdCreateReqJSON extends JSONModel{
        private final String householdName;
        private final String householdDescription;

        public HouseholdCreateReqJSON(String name, String description ) {
            householdName = name;
            householdDescription = description;
        }
    }

    public static class ErrorResponseJSON{
        public final int errorCode;
        public final String errorName;
        public final int httpStatus;
        public final String errorDescription;

        public ErrorResponseJSON(int errorCode, String errorName, int httpStatus, String errorDescription) {
            this.errorCode = errorCode;
            this.errorName = errorName;
            this.httpStatus = httpStatus;
            this.errorDescription = errorDescription;
        }
    }
}
