package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.OrderedBuilder;
import de.cubeisland.cubeengine.core.storage.database.QueryBuilder;
import de.cubeisland.cubeengine.core.util.StringUtils;

/**
 *
 * @author Anselm Brehme
 */
public abstract class MySQLOrderedBuilder<T> extends MySQLConditionalBuilder<T> implements OrderedBuilder<T>
{
    public T orderBy(String... cols)
    {
        query.append("ORDER BY ").append(StringUtils.implode(",", database.quote(cols))).append(" ");
        return (T)this;
    }

    public T limit(int n)
    {
        query.append("LIMIT ").append(n).append(" ");
        return (T)this;
    }

    public T offset(int n)
    {
        query.append("OFFSET ").append(n).append(" ");
        return (T)this;
    }

    public QueryBuilder end()
    {
        queryBuilder.query.append(query.toString());
        return queryBuilder;
    }
}