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
}
