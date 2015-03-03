package com.example.android.virtualpantry.Data;

/**
 * Created by Garrett on 3/2/2015.
 */
public class JSONModels {
    public static class RegisterReqJSON {
        private final String emailAddress;
        private final String password;
        private final String firstName;
        private final String lastName;
        public RegisterReqJSON (String emailAddress, String password, String firstName, String lastName) {
            this.emailAddress = emailAddress;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    public static class LoginReqJSON {
        private final String emailAddress;
        private final String password;
        public LoginReqJSON (String emailAddress, String password) {
            this.emailAddress = emailAddress;
            this.password = password;
        }
    }
}
