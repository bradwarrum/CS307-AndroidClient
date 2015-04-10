package com.example.android.virtualpantry.Data;

import android.app.LauncherActivity;

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

    public static class ListCreateReqJSON extends JSONModel {
        private final String listName;
        public ListCreateReqJSON (String name) {
            listName = name;
        }
    }

    public static class GetShoppingListResJSON extends JSONModel{
        public final long version;
        public final String name;
        public final List<ItemJSON> items;

        public GetShoppingListResJSON(long version, String name, List<ItemJSON> items){
            this.version = version;
            this.name = name;
            this.items = items;
        }

        public static class ItemJSON {
            public final String UPC;
            public final String description;
            public final int quantity;
            public final int fractional;
            public final ListItemPackagingJSON packaging;

            public ItemJSON(String UPC, String description, int quantity, int fractional, String unitName, ListItemPackagingJSON packaging){
                this.UPC = UPC;
                this.description = description;
                this.quantity = quantity;
                this.fractional = fractional;
                this.packaging = packaging;
            }

            public static class ListItemPackagingJSON {
                public final float packageSize;
                public final int unitID;
                public final String unitName;
                public final String unitAbbreviation;
                public final String packageName;

                public ListItemPackagingJSON(float packageSize, int unitID,
                                                  String unitName, String unitAbbreviation,
                                                  String packageName){
                    this.packageSize = packageSize;
                    this.unitID = unitID;
                    this.unitName = unitName;
                    this.unitAbbreviation = unitAbbreviation;
                    this.packageName = packageName;
                }
            }
        }
    }

    public static class LinkReqJSON extends JSONModel {
        public final String description;
        public final String packageName;
        public final int packageUnits;
        public final float packageSize;

        public LinkReqJSON(String description, String packageName, int packageUnits, float packageSize) {
            this.description = description;
            this.packageName = packageName;
            this.packageUnits = packageUnits;
            this.packageSize = packageSize;
        }
    }

    public static class CreateUPCResJSON{
        public final String UPC;

        public CreateUPCResJSON(String UPC){
            this.UPC = UPC;
        }
    }

    public static class UpdateListJSON extends JSONModel{
        public final long version;
        public final List<UpdateListItem> items;

        public UpdateListJSON(long version, List<UpdateListItem> items){
            this.version = version;
            this.items = items;
        }

        public static class UpdateListItem{
            public final String UPC;
            public final int quantity;
            public final int fractional;

            public UpdateListItem(String UPC, int quantity, int fractional){
                this.UPC = UPC;
                this.quantity = quantity;
                this.fractional = fractional;
            }
        }
    }

    public static class GetInventoryResJSON{
        public final long version;
        public final List<InventoryItemJSON> items;

        public GetInventoryResJSON(long version, List<InventoryItemJSON> items){
            this.version = version;
            this.items = items;
        }

        public static class InventoryItemJSON extends JSONModel{
            public final String UPC;
            public final boolean isInternalUPC;
            public final String description;
            public final int quantity;
            public final int fractional;
            public final InventoryItemPackagingJSON packaging;

            public InventoryItemJSON(String UPC, boolean isInternalUPC,
                                     String description, int quantity,
                                     int fractional, InventoryItemPackagingJSON packaging){
                this.UPC = UPC;
                this.isInternalUPC = isInternalUPC;
                this.description = description;
                this.quantity = quantity;
                this.fractional = fractional;
                this.packaging = packaging;
            }

            public static class InventoryItemPackagingJSON {
                public final float packageSize;
                public final int unitID;
                public final String unitName;
                public final String unitAbbreviation;
                public final String packageName;

                public InventoryItemPackagingJSON(float packageSize, int unitID,
                                                  String unitName, String unitAbbreviation,
                                                  String packageName){
                    this.packageSize = packageSize;
                    this.unitID = unitID;
                    this.unitName = unitName;
                    this.unitAbbreviation = unitAbbreviation;
                    this.packageName = packageName;
                }
            }
        }
    }

    public static class UpdateInventoryReqJSON extends JSONModel{
        public final long version;
        public final List<UpdateInventoryItem> items;

        public UpdateInventoryReqJSON(long version, List<UpdateInventoryItem> items){
            this.version = version;
            this.items = items;
        }

        public static class UpdateInventoryItem{
            public final String UPC;
            public final int quantity;
            public final int fractional;

            public UpdateInventoryItem(String UPC, int quantity, int fractional){
                this.UPC = UPC;
                this.quantity = quantity;
                this.fractional = fractional;
            }
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
