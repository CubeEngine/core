package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.LockBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLLockBuilder extends MySQLComponentBuilder<LockBuilder> implements LockBuilder
{
    private boolean multiple = false;

    public MySQLLockBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    public LockBuilder lock()
    {
        this.query.append("LOCK TABLES ");
        return this;
    }

    public LockBuilder table(String table)
    {
        if (multiple)
        {
            this.query.append(", ");

        }
        multiple = true;
        this.query.append(this.database.prepareName(table));
        return this;
    }

    public LockBuilder read()
    {
        this.query.append(" READ");
        return this;
    }

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
