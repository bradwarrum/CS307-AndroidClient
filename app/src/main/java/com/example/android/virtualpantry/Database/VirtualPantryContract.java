package com.example.android.virtualpantry.Database;

import android.provider.BaseColumns;

/**
 * Created by Garrett on 4/1/2015.
 */

//describes the database table setups
public class VirtualPantryContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.virtualpantry.app";

    public static final String PATH_LISTS = "lists";
    public static final String PATH_HOUSEHOULDS = "households";
    public static final String PATH_LIST_ITMES = "itmes";
    public static final String PATH_INVENTORY_ITEMS = "inventory";

    public static final class ShoppingListEntry implements BaseColumns {

        public static final String TABLE_NAME = "shopping_lists";

        public static final String COLUMN_LIST_ID = "list_id";

        public static final String COLUMN_LIST_NAME = "name";

        public static final String COLUMN_LIST_VERSION = "version";
    }

    public static final class HouseholdEntry implements BaseColumns {

        public static final String TABLE_NAME = "households";

        public static final String COLUMN_HOUSEHOLD_ID = "household_id";

        public static final String COLUMN_HOUSEHOLD_NAME = "name";

        public static final String COLUMN_HOUSEHOLD_DESCRIPTION = "description";
    }

    public static final class ShoppingListItemEntry implements BaseColumns {

        public static final String TABLE_NAME = "shopping_list_items";

        public static final String COLUMN_LIST_ID = "list_id";

        public static final String COLUMN_UPC = "item_upc";

        public static final String COLUMN_DESCRIPTION = "description";

        public static final String COLUMN_QUANTITY = "quantity";

        public static final String COLUMN_PACKAGE_NAME = "package_name";

        public static final String COLUMN_IN_CART = "in_cart";
    }

    public static final class InventoryEntry implements  BaseColumns {

        public static final String TABLE_NAME = "inventory_table";

        public static final String COLUMN_UPC = "item_upc";

        public static final String COLUMN_DESCRIPTION = "description";

        public static final String COLUMN_PACKAGE_SIZE = "package_size";

        public static final String COLUMN_PACKAGE_UNITS = "package_units";

        public static final String COLUMN_PACKAGE_NAME = "package_name";

        public static final String COLUMN_QUANTITY = "quantity";

        public static final String COLUMN_FRACTIONAL = "fractional";
    }


}
