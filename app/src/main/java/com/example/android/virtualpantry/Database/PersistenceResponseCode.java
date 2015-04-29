package com.example.android.virtualpantry.Database;

/**
 * Created by Brad on 4/20/2015.
 */

/**
 * This class enumerates response codes retrieved from the server as well as codes reserved for internal use.<br/>
 * Methods in this class require contiguous backing values from 0 through SERVER_BLOCK_END.  Do not make modifications to this enumeration unless you are sure there are no repercussions in doing so.<br/>
 * Enumeration values backed by the values starting with SERVER_BLOCK_START and SERVER_BLOCK_END are dictated by the server and must remain constant.  Enumeration values MUST exist for ordinal values 0 through SERVER_BLOCK_START - 1.
 */
public enum PersistenceResponseCode {
    SUCCESS(0),
    ERR_CLIENT_CONNECT(1),
    ERR_DB_INTERNAL(2),
    ERR_SERVER_INTERNAL(3),
    ERR_TOKEN_EXPIRED(4),
    ERR_INVALID_TOKEN(5),
    ERR_EMAIL_TAKEN(6),
    ERR_INVALID_PASSWORD(7),
    ERR_INVALID_PAYLOAD(8),
    ERR_USER_NOT_FOUND(9),
    ERR_HOUSEHOLD_NOT_FOUND(10),
    ERR_LIST_NOT_FOUND(11),
    ERR_ITEM_NOT_FOUND(12),
    ERR_UPC_FORMAT_NOT_SUPPORTED(13),
    ERR_UPC_CHECKSUM_INVALID(14),
    ERR_INSUFFICIENT_PERMISSIONS(15),
    ERR_OUTDATED_TIMESTAMP(16),
    ERR_RECIPE_NOT_FOUND(17),
    ERR_ITEM_DUPLICATE_FOUND(18),
    ERR_DB_DATA_NOT_FOUND(Integer.MAX_VALUE - 1),
    ERR_SERVER_MALFORMED_RESPONSE(Integer.MAX_VALUE);

    private static final int SERVER_BLOCK_START = 3;
    private static final int SERVER_BLOCK_END = 18;
    private static PersistenceResponseCode[] values = PersistenceResponseCode.values();

    private final int code;

    private PersistenceResponseCode(int code) {
        this.code = code;
    }

    public int getBackingNumeral() {
        return code;
    }

    /* This method is somewhat dangerous, ensure contiguous ordering on the enum type*/
    public static PersistenceResponseCode fromBackingCode (int code) {
        if (code < SERVER_BLOCK_START || code > SERVER_BLOCK_END) return null;
        return values[code];
    }


}
