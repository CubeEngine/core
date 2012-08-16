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
        this.query = new StringBuilder();
        this.query.append("SELECT ");
    }

    public SelectBuilder cols(String... cols)
    {
        this.query.append(StringUtils.implode(",", this.database.quote(cols))).append(" ");
        return this;
    }

    public SelectBuilder from(String... tables)
    {
        this.query.append("FROM ").append(StringUtils.implode(",", this.database.quote(tables))).append(" ");
        return this;
    }
}