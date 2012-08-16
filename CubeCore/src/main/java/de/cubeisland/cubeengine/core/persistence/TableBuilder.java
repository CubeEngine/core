package de.cubeisland.cubeengine.core.persistence;

import de.cubeisland.cubeengine.core.persistence.database.Database;

/**
 *
 * @author Anselm Brehme
 */
public interface TableBuilder
{
    
    
    public TableBuilder attribute(String name, AttrType type);

    public TableBuilder attribute(String name, AttrType type, int n);
    
    public TableBuilder unsigned();

    public TableBuilder next();
        
    public TableBuilder notNull();

    public TableBuilder nulL();

    public TableBuilder autoincrement();
    
    public TableBuilder primaryKey(String key);
    
    public TableBuilder foreignKey(String key);

    public TableBuilder references(String table, String key);
}
