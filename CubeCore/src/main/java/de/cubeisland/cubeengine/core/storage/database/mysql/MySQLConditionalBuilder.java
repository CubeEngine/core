package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.ConditionalBuilder;
import de.cubeisland.cubeengine.core.storage.database.Database;

/**
 *
 * @author Anselm Brehme
 */
public abstract class MySQLConditionalBuilder<T> implements ConditionalBuilder<T>
{
    protected Database database;
    protected StringBuilder query;
    protected MySQLQueryBuilder queryBuilder;
    
    private int subDepth = 0;

    public T beginWhere()
    {
        this.query.append("WHERE ");
        return (T)this;
    }

    public T col(String col)
    {
        this.query.append(database.quote(col));
        return (T)this;
    }

    public T value()
    {
        this.query.append("? ");
        return (T)this;
    }

    public T op(int operation)
    {
        switch (operation)
        {
            case 1:
                this.query.append("=");
                break;
            case 2:
                this.query.append("!=");
                break;
            case 3:
                this.query.append("<");
                break;
            case 4:
                this.query.append("<=");
                break;
            case 5:
                this.query.append(">");
                break;
            case 6:
                this.query.append(">=");
                break;
            default:
                throw new IllegalStateException("Invalid Operation");
        }
        return (T)this;
    }

    public T not()
    {
        this.query.append("NOT ");
        return (T)this;
    }

    public T and()
    {
        this.query.append("AND ");
        return (T)this;
    }

    public T or()
    {
        this.query.append("OR ");
        return (T)this;
    }

    public T beginSub()
    {
        this.query.append("(");
        ++this.subDepth;
        
        return (T)this;
    }

    public T endSub()
    {
        if (this.subDepth > 0)
        {
            this.query.append(")");
            --this.subDepth;
        }
        return (T)this;
    }

    public T endWhere()
    {
        while (this.subDepth > 0)
        {
            this.endSub();
        }
        return (T)this;
    }
}