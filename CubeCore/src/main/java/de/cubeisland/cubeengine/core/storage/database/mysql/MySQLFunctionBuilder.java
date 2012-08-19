package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.FunctionBuilder;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLFunctionBuilder<T extends MySQLConditionalBuilder> extends MySQLBuilderBase implements FunctionBuilder
{
    private int subDepth = 0;
    protected MySQLConditionalBuilder parent;

    public MySQLFunctionBuilder(T parent, MySQLQueryBuilder builder)
    {
        super(builder);
        this.parent = parent;
    }

    public FunctionBuilder beginFunction(String function)
    {
        this.query.append(" ").append(function.toUpperCase()).append("(");
        return this;
    }

    public FunctionBuilder function(String function)
    {
        this.query.append(" ").append(function.toUpperCase()).append("()");
        return this;
    }

    public MySQLFunctionBuilder as(String name)
    {
        this.query.append(" AS ").append(this.prepareName(name, false));
        return this;
    }

    public MySQLFunctionBuilder groupBy(String... cols)
    {
        this.query.append(" GROUP BY ").append(this.prepareName(cols[0], false));//TODO multiple cols
        return this;
    }

    public MySQLFunctionBuilder having()
    {
        this.query.append(" HAVING ");
        return this;
    }

    public MySQLFunctionBuilder field(String col)
    {
        this.query.append(this.prepareName(col, false));
        return this;
    }

    public MySQLFunctionBuilder is(int operation)
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

    public MySQLFunctionBuilder value()
    {
        this.query.append('?');
        return this;
    }

    public MySQLFunctionBuilder value(String name)
    {
        this.query.append(name);
        return this;
    }

    public MySQLFunctionBuilder not()
    {
        this.query.append(" NOT");
        return this;
    }

    public MySQLFunctionBuilder and()
    {
        this.query.append(" AND");
        return this;
    }

    public MySQLFunctionBuilder or()
    {
        this.query.append(" OR");
        return this;
    }

    public MySQLFunctionBuilder beginSub()
    {
        this.query.append('(');
        ++this.subDepth;
        return this;
    }

    public MySQLFunctionBuilder endSub()
    {
        if (this.subDepth > 0)
        {
            this.query.append(')');
            --this.subDepth;
        }
        return this;
    }

    public FunctionBuilder comma()
    {
        this.query.append(',');
        return this;
    }

    public FunctionBuilder distinct()
    {
        this.query.append(" DISTINCT");
        return this;
    }

    public FunctionBuilder wildcard()
    {
        this.query.append('*');
        return this;
    }

    public FunctionBuilder endFunction()
    {
        this.query.append(')');
        return this;
    }

    public FunctionBuilder where()
    {
        this.query.append(" WHERE");
        return this;
    }

    public T endFunctions()
    {
        this.parent.query.append(this.query);
        return (T)this.parent;
    }
}