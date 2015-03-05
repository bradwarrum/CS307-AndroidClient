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
            households = new ArrayList<HouseholdShortJSON>();
            in.readTypedList(households, HouseholdShortJSON.CREATOR);
        }
    }

    public static class HouseholdShortJSON implements Parcelable {
        public final long householdID;
        public final String householdName;
        public final String householdDescription;

        public HouseholdShortJSON(long householdID, String householdName, String householdDescription) {
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

        public static final Parcelable.Creator<HouseholdShortJSON> CREATOR =
            new Parcelable.Creator<HouseholdShortJSON>() {
                public HouseholdShortJSON createFromParcel(Parcel in) {
                    return new HouseholdShortJSON(in);
                }

                public HouseholdShortJSON[] newArray(int size) {
                    return new HouseholdShortJSON[size];
                }
            };

        private HouseholdShortJSON(Parcel in) {
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

    /*public static class GetListResJSON{
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
        public final String UPC;
        public final String description;
        public final int quantity;
        public final int fractional;
        public final String unitName;

        public ItemJSON(String UPC, String description, int quantity, int fractional, String unitName){
            this.UPC = UPC;
            this.description = description;
            this.quantity = quantity;
            this.fractional = fractional;
            this.unitName = unitName;
        }
    }*/

    public static class LinkReqJSON {
        public final String description;
        public final String unitName;

        public LinkReqJSON(String description, String units) {
            this.description = description;
            this.unitName = units;
        }
    }
    
    public static class HouseholdJSON{
        public final long householdId;
        public final String householdName;
        public final String householdDescription;
        public final String headOfHousehold;
        public final List<HouseholdMemberJSON> members;
        public final List<HouseholdListJSON> lists;

        public HouseholdJSON(long householdId, String householdName, String householdDescription, String headOfHousehold,
                             List<HouseholdMemberJSON> members, List<HouseholdListJSON> lists) {
            this.householdId = householdId;
            this.householdName = householdName;
            this.householdDescription = householdDescription;
            this.headOfHousehold = headOfHousehold;
            this.members = members;
            this.lists = lists;
        }
    }
    
    public static class HouseholdMemberJSON{
        public final String userID;
        public final String firstName;
        public final String lastName;
        public final String emailAddress;

        public HouseholdMemberJSON(String userID, String firstName, String lastName, String emailAddress){
            this.userID = userID;
            this.firstName = firstName;
            this.lastName = lastName;
            this.emailAddress = emailAddress;
        }
    }
    
    public static class HouseholdListJSON{
        public final long listID;
        public final String listName;

        public HouseholdListJSON(long listID, String listName) {
            this.listID = listID;
            this.listName = listName;
        }
    }

    public static class ListCreateReqJSON {
        private final String listName;
        public ListCreateReqJSON (String name) {
            listName = name;
        }
    }

    public static class GetShoppingListResJSON{
        private final long version;
        private final String name;
        private final List<Item> items;

        public GetShoppingListResJSON(long version, String name, List<Item> items){
            this.version = version;
            this.name = name;
            this.items = items;
        }

        public static class Item{
            private final String UPC;
            private final String description;
            private final int quantity;
            private final int fractional;
            private final String unitName;

            public Item(String UPC, String description, int quantity, int fractional, String unitName){
                this.UPC = UPC;
                this.description = description;
                this.quantity = quantity;
                this.fractional = fractional;
                this.unitName = unitName;
            }
        }
    }


}
