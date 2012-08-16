package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.InsertBuilder;
import de.cubeisland.cubeengine.core.util.StringUtils;

/**
 *
 * @author Anselm Brehme
 */
class MySQLInsertBuilder implements InsertBuilder
{
    private StringBuilder query;
    private MySQLQueryBuilder queryBuilder;
    private Database database;

    public MySQLInsertBuilder(MySQLQueryBuilder querybuilder)
    {
        this.queryBuilder = querybuilder;
        this.database = querybuilder.database;
        this.query = new StringBuilder("INSERT ");
    }

    public InsertBuilder into(String... tables)
    {
        this.query.append("INTO ").append(StringUtils.implode(",", database.quote(tables))).append(" ");
        return this;
    }

    public InsertBuilder cols(String... cols)
    {
        this.query.append("(").append(StringUtils.implode(",", database.quote(cols))).append(") ");
        return this;
    }

    public InsertBuilder values(int n)
    {
        this.query.append("VALUES").append("(?").append(StringUtils.repeat(",?", n - 1)).append(") ");
        return this;
    }

    @Override
    public MySQLQueryBuilder end()
    {
        this.queryBuilder.query.append(query.toString());
        return this.queryBuilder;
    }
}