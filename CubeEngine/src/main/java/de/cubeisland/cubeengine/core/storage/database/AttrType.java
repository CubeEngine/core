package de.cubeisland.cubeengine.core.storage.database;

/**
 * The possible AttributeTypes for the database.
 */
public enum AttrType
{

    // Numbers
    /**
     * -2147483648 to 2147483647 normal. 0 to 4294967295 UNSIGNED.
     */
    INT(
        "INT",
        false,
        true),
    /**
     * -128 to 127 normal. 0 to 255 UNSIGNED.
     */
    TINYINT(
        "TINYINT",
        false,
        true),
    /**
     * -32768 to 32767 normal. 0 to 65535 UNSIGNED.
     */
    SMALLINT(
        "SMALLINT",
        false,
        true),
    /**
     * -8388608 to 8388607 normal. 0 to 16777215 UNSIGNED.
     */
    MEDIUMINT(
        "MEDIUMINT",
        false,
        true),
    /**
     * -9223372036854775808 to 9223372036854775807 normal. 0 to 18446744073709551615 UNSIGNED.
     */
    BIGINT(
        "BIGINT",
        false,
        true),
    DECIMAL(
        "DECIMAL",
        false,
        true),
    FLOAT(
        "FLOAT",
        false,
        true),
    DOUBLE(
        "DOUBLE",
        false,
        true),
    REAL(
        "REAL",
        false,
        true),
    BIT(
        "BIT",
        false,
        true),
    BOOLEAN(
        "BOOLEAN",
        false,
        false),
    SERIAL(
        "SERIAL",
        true,
        true),
    // Date/Time:
    DATE(
        "DATE",
        false,
        false),
    TIME(
        "TIME",
        false,
        false),
    DATETIME(
        "DATETIME",
        false,
        false),
    TIMESTAMP(
        "TIMESTAMP",
        false,
        false),
    YEAR(
        "YEAR",
        false,
        false),
    // Strings
    CHAR(
        "CHAR",
        false,
        true),
    /**
     * Up to 255 characters.
     */
    VARCHAR(
        "VARCHAR",
        true,
        false),
    /**
     * Up to 65,535 characters.
     */
    TEXT(
        "TEXT",
        false,
        false),
    /**
     * Up to 255 characters.
     */
    TINYTEXT(
        "TINYTEXT",
        true,
        false),
    /**
     * Up to 16,777,215 characters.
     */
    MEDIUMTEXT(
        "MEDIUMTEXT",
        true,
        false),
    /**
     * Up to 4,294,967,295 characters.
     */
    LONGTEXT(
        "LONGTEXT",
        true,
        false),
    // Binary
    BINARY(
        "BINARY",
        false,
        false),
    VARBINARY(
        "VARBINARY",
        true,
        false),
    TINYBLOB(
        "TINYBLOB",
        false,
        false),
    /**
     * Up 16,777,215 bytes of data.
     */
    MEDIUMBLOB(
        "MEDIUMBLOB",
        false,
        false),
    /**
     * Up 65,535 bytes of data.
     */
    BLOB(
        "BLOB",
        false,
        false),
    /**
     * Up 4,294,967,295 bytes of data.
     */
    LONGBLOB(
        "LONGBLOB",
        false,
        false),
    ENUM(
        "ENUM",
        true,
        false), //TODO enum should work!!!
    SET(
        "SET",
        true,
        false);
    private final String  type;
    private final boolean length;
    private final boolean signed;

    private AttrType(String type, boolean length, boolean signed)
    {
        this.type = type;
        this.length = length;
        this.signed = signed;
    }

    public String getType()
    {
        return this.type;
    }

    public boolean hasLength()
    {
        return this.length;
    }

    public boolean canBeSigned()
    {
        return this.signed;
    }
}
