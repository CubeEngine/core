package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.LockBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;

/**
 * MYSQLQueryBuilder for locking tables.
 */
public class MySQLLockBuilder extends MySQLComponentBuilder<LockBuilder> implements LockBuilder
{
    private boolean multiple = false;

    public MySQLLockBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    @Override
    public LockBuilder lock()
    {
        this.query.append("LOCK TABLES ");
        return this;
    }

    @Override
    public LockBuilder table(String table)
    {
        if (multiple)
        {
            this.query.append(", ");

        }
        multiple = true;
        this.query.append(this.database.prepareTableName(table));
        return this;
    }

    @Override
    public LockBuilder read()
    {
        this.query.append(" READ");
        return this;
    }

    @Override
    public LockBuilder write()
    {
        this.query.append(" WRITE");
        return this;
    }

    @Override
    public QueryBuilder end()
    {
        this.multiple = false;
        return super.end();
    }
}
