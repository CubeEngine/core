package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.ConditionalBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.WhereBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
public abstract class MySQLConditionalBuilder<T extends ConditionalBuilder> extends MySQLBuilderBase implements ConditionalBuilder<T>
{
    private WhereBuilder<T> whereBuilder;

    protected MySQLConditionalBuilder(MySQLQueryBuilder builder)
    {
        super(builder);
        this.whereBuilder = null;
    }

    public WhereBuilder<T> beginWhere()
    {
        if (this.whereBuilder == null)
        {
            this.whereBuilder = new MySQLWhereBuilder<T>((T)this);
        }
        return this.whereBuilder;
    }
    
    public T orderBy(String... cols)
    {
        Validate.notEmpty(cols, "No cols specified!");
        
        this.query.append(" ORDER BY ").append(this.prepareColName(cols[0]));
        for (int i = 1; i < cols.length; ++i)
        {
            this.query.append(',').append(this.prepareColName(cols[i]));
        }
        return (T)this;
    }

    public T limit(int n)
    {
        this.query.append(" LIMIT ").append(n);
        return (T)this;
    }

    public T offset(int n)
    {
        this.query.append(" OFFSET ").append(n);
        return (T)this;
    }
}