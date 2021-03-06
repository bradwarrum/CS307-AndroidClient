package com.example.android.virtualpantry.Database;

import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by Garrett on 4/6/2015.
 */
public class PreferencesHelper {

    public static final String USER_INFO = "UserInfoPrefs";
    public static final String SHOPPING_CART = "ShoppingCart";

    //login info
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String TOKEN = "token";

    //shopping cart
    public static final String SHOPPING_CART_LIST_ID = "listID";
    public static final String SHOPPING_CART_HOUSEHOLD_ID = "householdID";
    public static final String SHOPPING_CART_ITEMS_IN_CART = "itemsInCart";

    public static final String NULL_PREFERENCE_VALUE = "null_preference";

    public static boolean isNull(String value){
        if(TextUtils.isEmpty(value) | value.equals(NULL_PREFERENCE_VALUE)){
            return true;
        }
        return false;
    }



}
