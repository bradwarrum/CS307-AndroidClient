package com.example.android.virtualpantry.Database;

/**
 * Created by Brad on 4/23/2015.
 */
public enum UnitTypes {
    POUNDS(1,"pounds","lb"),
    OUNCES(2,"ounces","oz"),
    GRAMS(3,"grams","g"),
    MILLIGRAMS(4,"milligrams", "mg"),
    LITERS(5, "liters", "L"),
    MILLILITERS(6, "milliliters", "mL"),
    GALLONS (7, "gallons", "gal"),
    QUARTS(8, "quarts","qt"),
    PINTS(9, "pints", "pt"),
    CUPS(10, "cups", "c"),
    TEASPOONS(11, "teaspoons", "tsp"),
    TABLESPOONS(12, "tablespoons", "tbsp"),
    FLUID_OUNCES(13,"fluid ounces", "fl oz"),
    UNITS(14, "units", "units");

    private int unitID;
    private String unitName, unitAbbrev;

    private static UnitTypes[] types = UnitTypes.values();

    private UnitTypes(int unitID, String unitName, String unitAbbrev) {
        this.unitID = unitID;
        this.unitName = unitName;
        this.unitAbbrev = unitAbbrev;
    }

    public int getUnitID() {
        return unitID;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getUnitAbbrev() {
        return unitAbbrev;
    }

    public static UnitTypes fromID(int unitID) {
        if (unitID < 1 || unitID > 14 ) {
            return null;
        }
        return types[unitID - 1];
    }
}
