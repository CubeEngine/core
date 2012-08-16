package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Anselm Brehme
 */
public interface QueryBuilder
{
    public QueryBuilder select(String... col);

    public QueryBuilder insertInto(String table, String... col);

    public QueryBuilder delete();

    public QueryBuilder from(String... tables);

    public QueryBuilder where(String... conditions);

    public QueryBuilder values(int n);

    public QueryBuilder limit(int n);

    public QueryBuilder orderBy(String col);

    public QueryBuilder offset(int n);

    public TableBuilder createTable(String name, boolean ifNoExist);
    
    public String end();
    
    public QueryBuilder clear();
}
