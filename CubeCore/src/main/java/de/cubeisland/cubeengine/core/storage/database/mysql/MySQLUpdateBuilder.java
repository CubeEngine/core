package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.UpdateBuilder;
import de.cubeisland.cubeengine.core.util.StringUtils;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLUpdateBuilder extends MySQLOrderedBuilder<UpdateBuilder> implements UpdateBuilder
{
    public MySQLUpdateBuilder(MySQLQueryBuilder querybuilder)
    {
        this.queryBuilder = querybuilder;
        this.database = querybuilder.database;
        this.builder = this;
        this.query = new StringBuilder();
        query.append("UPDATE ");
    }
    

    public UpdateBuilder tables(String... tables)
    {
        query.append(StringUtils.implode(",", database.quote(tables))).append(" ");
        return this;
    }

    public UpdateBuilder beginSets(String col)
    {
        query.append("SET ")
            .append(database.quote(col)).append("=?");
        return this;
    }

    public UpdateBuilder set(String col)
    {
        query.append(",").append(database.quote(col)).append("=?");
        return this;
    }

    public UpdateBuilder endSets()
    {
        return this;
    }

    public UpdateBuilder values(String... cols)
    {
        cols = database.quote(cols);
        for (int i = 0 ; i< cols.length ; ++i)
        {
            cols[i]=cols[i]+"=VALUES("+cols[i]+")";
        }
        query.append(StringUtils.implode(",", cols));
        return this;
    }
}