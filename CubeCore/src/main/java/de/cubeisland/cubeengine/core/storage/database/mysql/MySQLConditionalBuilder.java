package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.ConditionalBuilder;
import de.cubeisland.cubeengine.core.storage.database.Database;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLConditionalBuilder<T> implements ConditionalBuilder<T>
{
    protected Database database;
    protected T builder;
    protected StringBuilder query;
    protected MySQLQueryBuilder queryBuilder;

    public T beginWhere()
    {
        query.append("WHERE ");
        return builder;
    }

    public T col(String col)
    {
        query.append(database.quote(col));
        return builder;
    }

    public T value()
    {
        query.append("? ");
        return builder;
    }

    public T op(int operation)
    {
        switch (operation)
        {
            case 1:
                query.append("=");
                return builder;
            case 2:
                query.append("<>");
                return builder;
            case 3:
                query.append("<");
                return builder;
            case 4:
                query.append("<=");
                return builder;
            case 5:
                query.append(">");
                return builder;
            case 6:
                query.append(">=");
                return builder;
            default:
                throw new IllegalStateException("Invalid Operation");
        }
    }

    public T not()
    {
        query.append("NOT ");
        return builder;
    }

    public T and()
    {
        query.append("AND ");
        return builder;
    }

    public T or()
    {
        query.append("OR ");
        return builder;
    }

    public T beginSub()
    {
        query.append("(");
        return builder;
    }

    public T endSub()
    {
        query.append(")");
        return builder;
    }

    public T endWhere()
    {
        return builder;
    }
}