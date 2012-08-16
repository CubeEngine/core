package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Anselm Brehme
 */
public interface QueryBuilder
{
    public InsertBuilder insert(String... cols);

    public SelectBuilder select(String... col);
    
    public UpdateBuilder update(String ... tables);

    public DeleteBuilder delete(String table);

    public TableBuilder createTable(String name, boolean ifNoExist);
    
    public QueryBuilder dropTable();
    
    public String end();
    
    public QueryBuilder clear();
}
