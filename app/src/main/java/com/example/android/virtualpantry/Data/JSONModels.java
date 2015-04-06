package com.example.android.virtualpantry.Data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
