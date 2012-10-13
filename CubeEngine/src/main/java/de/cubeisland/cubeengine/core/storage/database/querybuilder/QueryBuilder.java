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

    public LockBuilder lock();

    public QueryBuilder startTransaction();

    public QueryBuilder commit();

    public QueryBuilder rollback();

    public QueryBuilder unlockTables();

    public QueryBuilder nextQuery();

    public AlterTableBuilder alterTable(String table);

    public String end();
}