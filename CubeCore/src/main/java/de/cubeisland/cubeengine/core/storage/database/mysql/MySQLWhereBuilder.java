package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.WhereBuilder;

/**
 *
 * @author Phillip Schichtel
 */
public class MySQLWhereBuilder<T extends MySQLBuilderBase> extends MySQLBuilderBase implements WhereBuilder<T> //TODO change name and put WHERE elsewhere
{
    private MySQLBuilderBase parent;
    protected StringBuilder query;
    
    private int subDepth = 0;

    protected MySQLWhereBuilder(T parent)
    {
        super();
        this.parent = (MySQLBuilderBase)parent;
        this.query.append(" WHERE "); //TODO move this
    }

    public WhereBuilder<T> field(String col)
    {
        this.query.append(this.parent.prepareName(col, false));
        return this;
    }

    public WhereBuilder<T> is(int operation)
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
        return this;
    }

    public WhereBuilder<T> value()
    {
        this.query.append('?');
        return this;
    }

    public WhereBuilder<T> not()
    {
        this.query.append(" NOT");
        return this;
    }

    public WhereBuilder<T> and()
    {
        this.query.append(" AND");
        return this;
    }

    public WhereBuilder<T> or()
    {
        this.query.append(" OR");
        return this;
    }

    public WhereBuilder<T> beginSub()
    {
        this.query.append('(');
        ++this.subDepth;
        
        return this;
    }

    public WhereBuilder<T> endSub()
    {
        if (this.subDepth > 0)
        {
            this.query.append(')');
            --this.subDepth;
        }
        return this;
    }

    public T endWhere()
    {
        this.parent.query.append(this.query);
        return (T)this.parent;
    }

    @Override
    public QueryBuilder end()
    {
        throw new IllegalAccessError("Use endWhere() instead");
    }
    
    
}