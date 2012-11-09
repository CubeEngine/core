package de.cubeisland.cubeengine.core.storage.database;

/**
 * The possible AttributeTypes for the database.
 */
public enum AttrType
{
    // Numbers
    INT("INT", false, true),
    TINYINT("TINYINT", false, true),
    SMALLINT("SMALLINT", false, true),
    MEDIUMINT("MEDIUMINT", false, true),
    BIGINT("BIGINT", false, true),
    DECIMAL("DECIMAL", false, true),
    FLOAT("FLOAT", false, true),
    DOUBLE("DOUBLE", false, true),
    REAL("REAL", false, true),
    BIT("BIT", false, true),
    BOOLEAN("BOOLEAN", false, false),
    SERIAL("SERIAL", true, true),
    // Date/Time:
    DATE("DATE", false, false),
    TIME("TIME", false, false),
    DATETIME("DATETIME", false, false),
    TIMESTAMP("TIMESTAMP", false, false),
    YEAR("YEAR", false, false),
    // Strings
    CHAR("CHAR", false, true),
    VARCHAR("VARCHAR", true, false),
    TEXT("TEXT", false, false),
    TINYTEXT("TINYTEXT", true, false),
    MEDIUMTEXT("MEDIUMTEXT", true, false),
    LONGTEXT("LONGTEXT", true, false),
    // Binary
    BINARY("BINARY", false, false),
    VARBINARY("VARBINARY", true, false),
    TINYBLOB("TINYBLOB", false, false),
    MEDIUMBLOB("MEDIUMBLOB", false, false),
    BLOB("BLOB", false, false),
    LONGBLOB("LONGBLOB", false, false),
    ENUM("ENUM", true, false),
    SET("SET", true, false);
    private final String type;
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
