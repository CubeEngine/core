package de.cubeisland.cubeengine.core.persistence;

/**
 *
 * @author Anselm Brehme
 */
public enum AttrType
{
    INT("INT"),
    VARCHAR("VARCHAR"),
    TEXT("TEXT"),
    DATE("DATE"),
    //NUMERIC:
    TINYINT("TINYINT"),
    SMALLINT("SMALLINT"),
    MEDIUMINT("MEDIUMINT"),
    BIGINT("BIGINT"),
    
    DECIMAL("DECIMAL"),
    FLOAT("FLOAT"),
    DOUBLE("DOUBLE"),
    REAL("REAL"),
    
    BIT("BIT"),
    BOOLEAN("BOOLEAN"),
    SERIAL("SERIAL"),
    
    //DATE & TIME:
    DATETIME("DATETIME"),
    TIMESTAMP("TIMESTAMP"),
    TIME("TIME"),
    YEAR("YEAR"),
    
    //STRING:
    CHAR("CHAR"),
    
    TINYTEXT("TINYTEXT"),
    MEDIUMTEXT("MEDIUMTEXT"),
    LONGTEXT("LONGTEXT"),
    
    BINARY("BINARY"),
    VARBINARY("VARBINARY"),
    
    TINYBLOB("TINYBLOB"),
    MEDIUMBLOB("MEDIUMBLOB"),
    BLOB("BLOB"),
    LONGBLOB("LONGBLOB"),
    
    ENUM("ENUM"),
    SET("SET"),
    
    CUSTOM(""),
    ;
    
    public String type;
    
    private AttrType(String s)
    {
        this.type = s;
    }
}
