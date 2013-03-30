package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.ConditionalBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import org.apache.commons.lang.Validate;

/**
 * Abstract MYSQLlQueryBuilder used by other builders.
 */
public abstract class MySQLConditionalBuilder<This extends ConditionalBuilder>
        extends MySQLComponentBuilder<This> implements ConditionalBuilder<This>
{
    protected MySQLConditionalBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    private boolean orderBy = false;

    @Override
    @SuppressWarnings("unchecked")
    public This orderBy(String... cols)
    {
        Validate.notEmpty(cols, "No cols specified!");
        if (!orderBy)
        {
            orderBy = true;
            this.query.append(" ORDER BY ");
        }
        else
        {
            this.query.append(",");
        }
        this.query.append(this.database.prepareFieldName(cols[0]));
        for (int i = 1; i < cols.length; ++i)
        {
            this.query.append(',').append(this.database.prepareFieldName(cols[i]));
        }
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This limit(int n)
    {
        this.query.append(" LIMIT ").append(n);
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This limit()
    {
        this.query.append(" LIMIT ?");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This offset(int n)
    {
        this.query.append(" OFFSET ").append(n);
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This offset()
    {
        this.query.append(" OFFSET ?");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This where()
    {
        this.query.append(" \nWHERE ");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This between()
    {
        this.query.append(" BETWEEN ");
        return (This)this.value().and().value();
    }

    @Override
    public This between(Object val1, Object val2)
    {
        this.query.append(" BETWEEN ");
        return (This)this.value(val1).and().value(val2);
    }

    @Override
    @SuppressWarnings("unchecked")
    public This asc()
    {
        this.query.append(" ASC ");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This desc()
    {
        this.query.append(" DESC ");
        return (This)this;
    }

    @Override
    public QueryBuilder end()
    {
        this.orderBy = false;
        return super.end();
    }
}
