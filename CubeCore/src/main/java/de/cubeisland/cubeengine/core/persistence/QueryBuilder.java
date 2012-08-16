package de.cubeisland.cubeengine.core.persistence;

/**
 *
 * @author Anselm Brehme
 */
public interface QueryBuilder
{
    public QueryBuilder select(String... col);

    public QueryBuilder insertInto(String table, String... col);

    public QueryBuilder delete();

    public QueryBuilder from(String table);

    public QueryBuilder where(String... conditions);

    public QueryBuilder values(int n);

    public QueryBuilder limit(int n);

    public QueryBuilder orderBy(String col);

    public QueryBuilder offset(int n);

    public QueryBuilder createTable(TableBuilder tb, boolean ifNoExist);

    public QueryBuilder engine(String engine);

    public QueryBuilder defaultcharset(String charset);

    public QueryBuilder autoincrement(int n);

    
}
