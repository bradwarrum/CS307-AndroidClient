package com.example.android.virtualpantry.Data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

/**
 * Created by Garrett on 4/6/2015.
 */
public class JSONModels {

    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    //base class to allow class to be exported to String
    public static class JSONModel {
        public String toString(){
            return gson.toJson(this);
        }
    }

    public static class LoginRequest extends JSONModel{
        private final String emailAddress;
        private final String password;

        public LoginRequest(String emailAddress, String password) {
            this.emailAddress = emailAddress;
            this.password = password;
        }
    }

    public static class LoginResponse {
        public final String firstName;
        public final String lastName;
        public final int userID;
        public final String token;

        LoginResponse(String firstName, String lastName, int userID, String token){
            this.firstName = firstName;
            this.lastName = lastName;
            this.userID = userID;
            this.token = token;
        }
    }

    public static class RegisterRequest extends JSONModel{
        private final String emailAddress;
        private final String password;
        private final String firstName;
        private final String lastName;

        public RegisterRequest(String emailAddress, String password, String firstName, String lastName) {
            this.emailAddress = emailAddress;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    public static class UserInfoResponse {
        public final long userID;
        public final String firstName;
        public final String lastName;
        public final String emailAddress;
        public final List<Household> households;

        public UserInfoResponse(
                long userID,
                String firstName,
                String lastName,
                String emailAddress,
                List<Household> households) {
            this.userID = userID;
            this.firstName = firstName;
            this.lastName = lastName;
            this.emailAddress = emailAddress;
            this.households = households;
        }

        public static class Household {
            public final long householdID;
            public final String householdName;
            public final String householdDescription;

            public Household(long householdID, String householdName, String householdDescription) {
                this.householdID = householdID;
                this.householdName = householdName;
                this.householdDescription = householdDescription;
            }
        }
    }

    public static class HouseholdCreateRequest extends JSONModel{
        private final String householdName;
        private final String householdDescription;

        public HouseholdCreateRequest(String name, String description) {
            householdName = name;
            householdDescription = description;
        }
    }

    public static class HouseholdCreateResponse {
        public int householdID;
        public long version;
    }

    public static class Household {
        public final long householdId;
        public final String householdName;
        public final String householdDescription;
        public final String headOfHousehold;
        public final List<HouseholdMember> members;
        public final List<HouseholdList> lists;

        public Household(long householdId, String householdName, String householdDescription, String headOfHousehold,
                         List<HouseholdMember> members, List<HouseholdList> lists) {
            this.householdId = householdId;
            this.householdName = householdName;
            this.householdDescription = householdDescription;
            this.headOfHousehold = headOfHousehold;
            this.members = members;
            this.lists = lists;
        }

        public static class HouseholdMember {
            public final String userID;
            public final String firstName;
            public final String lastName;
            public final String emailAddress;

            public HouseholdMember(String userID, String firstName, String lastName, String emailAddress){
                this.userID = userID;
                this.firstName = firstName;
                this.lastName = lastName;
                this.emailAddress = emailAddress;
            }
        }

        public static class HouseholdList {
            public final long listID;
            public final String listName;

            public HouseholdList(long listID, String listName) {
                this.listID = listID;
                this.listName = listName;
            }
        }
    }

    public static class ListCreateRequest extends JSONModel {
        private final String listName;
        public ListCreateRequest(String name) {
            listName = name;
        }
    }

    public static class ListCreateResponse {
        public long version;
        public int listID;
    }

    public static class GetShoppingListResponse extends JSONModel{
        public final long version;
        public final String name;
        public final List<Item> items;

        public GetShoppingListResponse(long version, String name, List<Item> items){
            this.version = version;
            this.name = name;
            this.items = items;
        }

        public static class Item {
            public final String UPC;
            public final String description;
            public final int quantity;
            public final int fractional;
            public final int cartQuantity;
            public final int cartFractional;
            public final ListItemPackaging packaging;

            public Item(String UPC, String description, int quantity, int fractional, ListItemPackaging packaging){
                this(UPC, description, quantity, fractional, 0, 0, packaging);
            }

            public Item(String UPC, String description, int quantity, int cartQuantity, int cartFractional, int fractional, ListItemPackaging packaging){
                this.UPC = UPC;
                this.description = description;
                this.quantity = quantity;
                this.fractional = fractional;
                this.packaging = packaging;
                this.cartQuantity = cartQuantity;
                this.cartFractional = cartFractional;

            }

            public static class ListItemPackaging {
                public final float packageSize;
                public final int unitID;
                public final String unitName;
                public final String unitAbbreviation;
                public final String packageName;

                public ListItemPackaging(float packageSize, int unitID,
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

    public static class LinkRequest extends JSONModel {
        public final String description;
        public final String packageName;
        public final int packageUnits;
        public final float packageSize;

        public LinkRequest(String description, String packageName, int packageUnits, float packageSize) {
            this.description = description;
            this.packageName = packageName;
            this.packageUnits = packageUnits;
            this.packageSize = packageSize;
        }
    }

    public static class CreateUPCResponse {
        public final String UPC;

        public CreateUPCResponse(String UPC){
            this.UPC = UPC;
        }
    }

    public static class UpdateListRequest extends JSONModel{
        public final long version;
        public final List<UpdateListItem> items;

        public UpdateListRequest(long version, List<UpdateListItem> items){
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

    public static class GetInventoryResponse {
        public final long version;
        public final List<InventoryItem> items;

        public GetInventoryResponse(long version, List<InventoryItem> items){
            this.version = version;
            this.items = items;
        }

        public static class InventoryItem extends JSONModel{
            public final String UPC;
            public final boolean isInternalUPC;
            public final String description;
            public final int quantity;
            public final int fractional;
            public final InventoryItemPackaging packaging;

            public InventoryItem(String UPC, boolean isInternalUPC,
                                 String description, int quantity,
                                 int fractional, InventoryItemPackaging packaging){
                this.UPC = UPC;
                this.isInternalUPC = isInternalUPC;
                this.description = description;
                this.quantity = quantity;
                this.fractional = fractional;
                this.packaging = packaging;
            }

            public static class InventoryItemPackaging {
                public final float packageSize;
                public final int unitID;
                public final String unitName;
                public final String unitAbbreviation;
                public final String packageName;

                public InventoryItemPackaging(float packageSize, int unitID,
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

    public static class UpdateInventoryRequest extends JSONModel{
        public final long version;
        public final List<UpdateInventoryItem> items;

        public UpdateInventoryRequest(long version, List<UpdateInventoryItem> items){
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

    public static class UpdateInventoryResponse {
        public long version;
    }

    public static class ErrorResponse {
        public final int errorCode;
        public final String errorName;
        public final int httpStatus;
        public final String errorDescription;

        public ErrorResponse(int errorCode, String errorName, int httpStatus, String errorDescription) {
            this.errorCode = errorCode;
            this.errorName = errorName;
            this.httpStatus = httpStatus;
            this.errorDescription = errorDescription;
        }
    }
}
