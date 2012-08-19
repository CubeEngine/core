package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Anselm Brehme
 */
public interface QueryBuilder
{
    public InsertBuilder insert();
    
    public MergeBuilder merge();

    public SelectBuilder select();
    
    public UpdateBuilder update();

    public DeleteBuilder delete();

    public TableBuilder createTable(String name, boolean ifNoExist);
    
    public QueryBuilder clearTable(String table);
    
    public QueryBuilder dropTable(String... tables);
    
    public String endQuery();
    
    public QueryBuilder customSql(String sql);
}
