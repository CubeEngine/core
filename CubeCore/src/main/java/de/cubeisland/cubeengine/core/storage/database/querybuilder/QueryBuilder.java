package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Anselm Brehme
 */
public interface QueryBuilder
{
    public InsertBuilder insert();
    public MergeBuilder merge();
    public SelectBuilder select(String... tables);
    public UpdateBuilder update(String... tables);
    public DeleteBuilder delete();
    public TableBuilder createTable(String name, boolean ifNoExist);
    public QueryBuilder clearTable(String table);
    public QueryBuilder dropTable(String... tables);
    public String end();
}