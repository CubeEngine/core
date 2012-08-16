package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Anselm Brehme
 */
public interface QueryBuilder
{
    public InsertBuilder insert();

    public SelectBuilder select();
    
    public UpdateBuilder update();
    
    public UpdateBuilder onDuplicateUpdate();

    public DeleteBuilder delete();

    public TableBuilder createTable(String name, boolean ifNoExist);
    
    public QueryBuilder dropTable(String table);
    
    public String end();
    
    public QueryBuilder clear();
    
    public QueryBuilder initialize();
}
