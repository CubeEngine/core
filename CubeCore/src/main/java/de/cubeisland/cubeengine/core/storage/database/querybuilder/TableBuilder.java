package de.cubeisland.cubeengine.core.storage.database.querybuilder;

import de.cubeisland.cubeengine.core.storage.database.AttrType;

/**
 *
 * @author Anselm Brehme
 */
public interface TableBuilder<This extends TableBuilder, QueryBuilder> extends ComponentBuilder<This, QueryBuilder>
{
    
    public This beginFields();
    
    public This field(String name, AttrType type);
    
    public This field(String name, AttrType type, int length);
    
    public This field(String name, AttrType type, int length, boolean notnull);
    
    public This field(String name, AttrType type, boolean notnull);
    
    public This field(String name, AttrType type, int length, boolean notnull, boolean unsigned);
    
    public This field(String name, AttrType type, int length, boolean notnull, boolean unsigned, boolean ai);
    
    public This primaryKey(String key);
    
    public This foreignKey(String key);

    public This references(String table, String key);
    
    public This endFields();

    public This engine(String engine);

    public This defaultcharset(String charset);

    public This autoIncrement(int n);
}
