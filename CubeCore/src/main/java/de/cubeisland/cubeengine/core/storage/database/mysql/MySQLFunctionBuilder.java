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

    public FunctionBuilder where()
    {
        this.query.append("WHERE ");
        return this;
    }

    public MySQLFunctionBuilder<T> now()
    {
        this.query.append("NOW() ");
        return this;
    }

    public MySQLFunctionBuilder<T> avg(String col)
    {
        this.query.append("AVG(").append(this.prepareName(col, true)).append(") ");
        return this;
    }

    public MySQLFunctionBuilder<T> count(String col, boolean distinct)
    {
        this.query.append("COUNT(");
        if (distinct)
        {
            this.query.append("DISTINCT ");
        }
        this.query.append(this.prepareName(col, true)).append(") ");
        return this;
    }

    public MySQLFunctionBuilder count(String col)
    {
        return this.count(col, false);
    }

    public MySQLFunctionBuilder countall()
    {
        this.query.append("COUNT(*) ");
        return this;
    }

    public MySQLFunctionBuilder min(String col)
    {
        this.query.append("MIN(").append(this.prepareName(col, true)).append(") ");
        return this;
    }

    public MySQLFunctionBuilder max(String col)
    {
        this.query.append("MAX(").append(this.prepareName(col, true)).append(") ");
        return this;
    }

    public MySQLFunctionBuilder sum(String col)
    {
        this.query.append("SUM(").append(this.prepareName(col, true)).append(") ");
        return this;
    }

    public MySQLFunctionBuilder first(String col)
    {
        this.query.append("FIRST(").append(this.prepareName(col, true)).append(") ");
        return this;
    }

    public MySQLFunctionBuilder last(String col)
    {
        this.query.append("LAST(").append(this.prepareName(col, true)).append(") ");
        return this;
    }

    public MySQLFunctionBuilder ucase(String col)
    {
        this.query.append("UCASE(").append(this.prepareName(col, true)).append(") ");
        return this;
    }

    public MySQLFunctionBuilder lcase(String col)
    {
        this.query.append("LCASE(").append(this.prepareName(col, true)).append(") ");
        return this;
    }

    public MySQLFunctionBuilder mid(String col, int start, int length)
    {
        this.query.append("MID(").append(this.prepareName(col, true)).append(",").append(start).append(",").append(length).append(") ");
        return this;
    }

    public MySQLFunctionBuilder mid(String col, int start)
    {
        this.query.append("MID(").append(this.prepareName(col, true)).append(",").append(start).append(") ");
        return this;
    }

    public MySQLFunctionBuilder len(String col)
    {
        this.query.append("LEN(").append(this.prepareName(col, true)).append(") ");
        return this;
    }

    public MySQLFunctionBuilder round(String col, int decimals)
    {
        this.query.append("ROUND(").append(this.prepareName(col, true)).append(",").append(decimals).append(") ");
        return this;
    }

    public MySQLFunctionBuilder format(String col, String format)
    {
        this.query.append("FORMAT(").append(this.prepareName(col, true)).append(",").append(format).append(") ");
        return this;
    }

    public MySQLFunctionBuilder as(String name)
    {
        this.query.append("AS ").append(this.prepareName(name, true)).append(" ");
        return this;
    }

    public MySQLFunctionBuilder groupBy(String col)
    {
        this.query.append("GROUP BY ").append(this.prepareName(col, true)).append(" ");
        return this;
    }

    public MySQLFunctionBuilder having()
    {
        this.query.append("HAVING ");
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

    public T endFunction()
    {
        this.parent.query.append(this.query);
        return (T)this.parent;
    }
}