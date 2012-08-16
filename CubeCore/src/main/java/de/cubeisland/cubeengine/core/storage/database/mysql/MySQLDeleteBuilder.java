package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.DeleteBuilder;
import de.cubeisland.cubeengine.core.util.StringUtils;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLDeleteBuilder extends MySQLOrderedBuilder<DeleteBuilder> implements DeleteBuilder
{
    public MySQLDeleteBuilder(MySQLQueryBuilder querybuilder)
    {
        this.queryBuilder = querybuilder;
        this.database = querybuilder.database;
        this.builder = this;
        this.query = new StringBuilder();
        query.append("DELETE ");
    }

    public DeleteBuilder from(String... tables)
    {
        query.append("FROM ").append(StringUtils.implode(",", database.quote(tables))).append(" ");
        return this;
    }
}