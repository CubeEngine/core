package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;

/**
 *
 * @author Anselm Brehme
 */
public abstract class MySQLComponentBuilder<This extends ComponentBuilder> implements ComponentBuilder<This>
{
    protected StringBuilder query = new StringBuilder();
    protected Database database;
    private boolean inFunction = false;
    private boolean deepInFunction = false;
    protected Integer subDepth = 0;
    protected MySQLQueryBuilder parent;

    public MySQLComponentBuilder(MySQLQueryBuilder parent)
    {
        this.parent = parent;
        this.database = parent.database;
    }

    public This rawSQL(String sql)
    {
        this.query.append(sql);
        return (This)this;
    }

    public This function(String function)
    {
        this.query.append(" ").append(function).append("()");
        return (This)this;
    }

    public This beginFunction(String function)
    {
        this.query.append(" ").append(function).append("(");
        this.inFunction = true;
        return (This)this;
    }

    public This endFunction()
    {
        this.deepInFunction = false;
        this.inFunction = false;
        this.query.append(")");
        return (This)this;
    }

    public This endFunction(String function)
    {
        this.query.append(")");
        this.inFunction = false;
        return (This)this;
    }

    public This field(String field)
    {

        this.query.append(this.database.prepareFieldName(field));
        return (This)this;
    }

    public This value(Object value)
    {
        if (inFunction)
        {
            if (deepInFunction)
            {
                query.append(",");
            }
            else
            {
                deepInFunction = true;
            }
        }
        if (value instanceof String)
        {
            this.query.append(this.database.prepareString((String)value));
        }
        else
        {
            this.query.append(value.toString());
        }
        return (This)this;
    }

    public This is(Integer operation)
    {
        switch (operation)
        {
            case 1:
                this.query.append('=');
                break;
            case 2:
                this.query.append("!=");
                break;
            case 3:
                this.query.append('<');
                break;
            case 4:
                this.query.append("<=");
                break;
            case 5:
                this.query.append('>');
                break;
            case 6:
                this.query.append(">=");
                break;
            default:
                throw new IllegalStateException("Invalid operation");
        }
        return (This)this;
    }

    public This not()
    {
        this.query.append(" NOT");
        return (This)this;
    }

    public This and()
    {
        this.query.append(" AND");
        return (This)this;
    }

    public This or()
    {
        this.query.append(" OR");
        return (This)this;
    }

    public This beginSub()
    {
        this.query.append('(');
        ++this.subDepth;
        return (This)this;
    }

    public This endSub()
    {
        if (this.subDepth > 0)
        {
            this.query.append(')');
            --this.subDepth;
        }
        return (This)this;
    }

    public This wildcard()
    {
        this.query.append("*");
        return (This)this;
    }

    public This value()
    {
        this.query.append("?");
        return (This)this;
    }

    public This as(String field)
    {
        this.query.append(" AS ").append(this.database.prepareFieldName(field));
        return (This)this;
    }

    public This groupBy(String field)
    {
        this.query.append(" GROUP BY(").append(this.database.prepareFieldName(field)).append(")");
        return (This)this;
    }

    public This having()
    {
        this.query.append(" HAVING");
        return (This)this;
    }

    public QueryBuilder end()
    {
        this.parent.query.append(this.query);
        this.query = null;
        return this.parent;
    }
}