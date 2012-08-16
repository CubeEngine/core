package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Anselm Brehme
 */
public interface TableBuilder
{
    
    public TableBuilder startFields();
    
    public TableBuilder field(String name, AttrType type);
    
    public TableBuilder field(String name, AttrType type, int length);
    
    public TableBuilder field(String name, AttrType type, int length, boolean notnull);
    
    public TableBuilder field(String name, AttrType type, int length, boolean notnull, boolean unsigned);
    
    public TableBuilder field(String name, AttrType type, int length, boolean notnull, boolean unsigned, boolean ai);
    
    public TableBuilder endFields();
    
    public TableBuilder primaryKey(String key);
    
    public TableBuilder foreignKey(String key);

    public TableBuilder references(String table, String key);

    public TableBuilder engine(String engine);

    public TableBuilder defaultcharset(String charset);

    public TableBuilder autoIncrement(int n);
    
    public QueryBuilder endCreateTable();
}
