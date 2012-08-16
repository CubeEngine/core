package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.SelectBuilder;
import de.cubeisland.cubeengine.core.util.StringUtils;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLSelectBuilder extends MySQLOrderedBuilder<SelectBuilder> implements SelectBuilder
{
    public MySQLSelectBuilder(MySQLQueryBuilder querybuilder)
    {
        this.queryBuilder = querybuilder;
        this.database = querybuilder.database;
        this.builder = this;
        this.query = new StringBuilder();
        query.append("SELECT ");
    }

    public SelectBuilder cols(String... cols)
    {
        query.append(StringUtils.implode(",", database.quote(cols))).append(" ");
        return this;
    }

    public SelectBuilder from(String... tables)
    {
        query.append("FROM ").append(StringUtils.implode(",", database.quote(tables))).append(" ");
        return this;
    }
}