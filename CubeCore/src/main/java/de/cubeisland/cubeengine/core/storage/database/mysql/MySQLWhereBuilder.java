package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.ConditionalBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.WhereBuilder;

/**
 *
 * @author Phillip Schichtel
 */
public class MySQLWhereBuilder<T extends ConditionalBuilder> implements WhereBuilder<T>
{
    private MySQLConditionalBuilder parent;
    private final StringBuilder query;
    
    private int subDepth = 0;

    protected MySQLWhereBuilder(T parent)
    {
        this.parent = (MySQLConditionalBuilder)parent;
        this.query = new StringBuilder(" WHERE ");
    }

    public WhereBuilder<T> field(String col)
    {
        this.query.append(this.parent.prepareColName(col));
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

    public T end()
    {
        this.parent.query.append(this.query);
        
        return (T)this.parent;
    }
    
}
