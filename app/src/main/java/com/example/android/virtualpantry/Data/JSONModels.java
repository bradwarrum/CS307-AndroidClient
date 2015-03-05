package com.example.android.virtualpantry.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Garrett on 3/2/2015.
 */
public class JSONModels {
    public static class RegisterReqJSON {
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

    public static class LoginReqJSON {
        private final String emailAddress;
        private final String password;

        public LoginReqJSON(String emailAddress, String password) {
            this.emailAddress = emailAddress;
            this.password = password;
        }
    }

    public static class UserInfoResJSON implements Parcelable {
        public final long userID;
        public final String firstName;
        public final String lastName;
        public final String emailAddress;
        public final List<HouseholdJSON> households;

        public UserInfoResJSON(
                long userID,
                String firstName,
                String lastName,
                String emailAddress,
                List<HouseholdJSON> households) {
            this.userID = userID;
            this.firstName = firstName;
            this.lastName = lastName;
            this.emailAddress = emailAddress;
            this.households = households;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeLong(userID);
            out.writeString(firstName);
            out.writeString(lastName);
            out.writeString(emailAddress);
            out.writeList(households);
        }

        public static final Parcelable.Creator<UserInfoResJSON> CREATOR =
                new Parcelable.Creator<UserInfoResJSON>() {
                    public UserInfoResJSON createFromParcel(Parcel in) {
                        return new UserInfoResJSON(in);
                    }

                    public UserInfoResJSON[] newArray(int size) {
                        return new UserInfoResJSON[size];
                    }
                };

        private UserInfoResJSON(Parcel in) {
            userID = in.readLong();
            firstName = in.readString();
            lastName = in.readString();
            emailAddress = in.readString();
            households = new ArrayList<HouseholdJSON>();
            in.readTypedList(households, HouseholdJSON.CREATOR);
        }
    }

    public static class HouseholdJSON implements Parcelable {
        public final long householdID;
        public final String householdName;
        public final String householdDescription;

        public HouseholdJSON(long householdID, String householdName, String householdDescription) {
            this.householdID = householdID;
            this.householdName = householdName;
            this.householdDescription = householdDescription;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeLong(householdID);
            out.writeString(householdName);
            out.writeString(householdDescription);
        }

        public static final Parcelable.Creator<HouseholdJSON> CREATOR =
            new Parcelable.Creator<HouseholdJSON>() {
                public HouseholdJSON createFromParcel(Parcel in) {
                    return new HouseholdJSON(in);
                }

                public HouseholdJSON[] newArray(int size) {
                    return new HouseholdJSON[size];
                }
            };

        private HouseholdJSON(Parcel in) {
            householdID = in.readLong();
            householdName = in.readString();
            householdDescription = in.readString();
        }
    }

    public static class HouseholdCreateReqJSON {
        private final String householdName;
        private final String householdDescription;
        public HouseholdCreateReqJSON(String name, String description ) {
            householdName = name;
            householdDescription = description;
        }
    }

    public static class GetListResJSON{
        private final long version;
        private final String name;
        private final List<ItemJSON> items;

        public GetListResJSON(long version, String name, List<ItemJSON> items){
            this.version = version;
            this.name = name;
            this.items = items;
        }
    }

    public static class ItemJSON{
        private final String UPC;
        private final String description;
        private final int quantity;
        private final int fractional;
        private final String unitName;

        public ItemJSON(String UPC, String description, int quantity, int fractional, String unitName){
            this.UPC = UPC;
            this.description = description;
            this.quantity = quantity;
            this.fractional = fractional;
            this.unitName = unitName;
        }
    }

    public static class LinkReqJSON {
        private final String description;
        private final String unitName;

        public LinkReqJSON(String description, String units) {
            this.description = description;
            this.unitName = units;
        }
    }
}
