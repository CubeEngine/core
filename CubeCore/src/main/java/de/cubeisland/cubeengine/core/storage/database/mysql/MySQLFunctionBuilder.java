package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.FunctionBuilder;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLFunctionBuilder<T extends MySQLConditionalBuilder> extends MySQLCompareBuilder implements FunctionBuilder
{
    
    public MySQLFunctionBuilder(T parent)
    {
        super(parent);
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

    public T endFunction()
    {
        this.parent.query.append(this.query);
        return (T)this.parent;
    }
}