package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.ConditionalBuilder;
import de.cubeisland.cubeengine.core.storage.database.FunctionBuilder;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLFunctionBuilder<T extends ConditionalBuilder> implements FunctionBuilder<T>
{
    private MySQLConditionalBuilder parent;
    private final StringBuilder query;

    protected MySQLFunctionBuilder(T parent)
    {
        this.parent = (MySQLConditionalBuilder)parent;
        this.query = new StringBuilder();
    }

    public FunctionBuilder<T> now()
    {
        this.query.append("NOW() ");
        return this;
    }

    public FunctionBuilder<T> avg(String col)
    {
        this.query.append("AVG(").append(this.parent.prepareName(col, true)).append(") ");
        return this;
    }

    public FunctionBuilder<T> count(String col, boolean distinct)
    {
        this.query.append("COUNT(");
        if (distinct)
        {
            this.query.append("DISTINCT ");
        }
        this.query.append(this.parent.prepareName(col, true)).append(") ");
        return this;
    }

    public FunctionBuilder<T> count(String col)
    {
        return this.count(col, false);
    }

    public FunctionBuilder<T> countall()
    {
        this.query.append("COUNT(*) ");
        return this;
    }

    public FunctionBuilder<T> min(String col)
    {
        this.query.append("MIN(").append(this.parent.prepareName(col, true)).append(") ");
        return this;
    }

    public FunctionBuilder<T> max(String col)
    {
        this.query.append("MAX(").append(this.parent.prepareName(col, true)).append(") ");
        return this;
    }

    public FunctionBuilder<T> sum(String col)
    {
        this.query.append("SUM(").append(this.parent.prepareName(col, true)).append(") ");
        return this;
    }

    public FunctionBuilder<T> first(String col)
    {
        this.query.append("FIRST(").append(this.parent.prepareName(col, true)).append(") ");
        return this;
    }

    public FunctionBuilder<T> last(String col)
    {
        this.query.append("LAST(").append(this.parent.prepareName(col, true)).append(") ");
        return this;
    }

    public FunctionBuilder<T> ucase(String col)
    {
        this.query.append("UCASE(").append(this.parent.prepareName(col, true)).append(") ");
        return this;
    }

    public FunctionBuilder<T> lcase(String col)
    {
        this.query.append("LCASE(").append(this.parent.prepareName(col, true)).append(") ");
        return this;
    }

    public FunctionBuilder<T> mid(String col, int start, int length)
    {
        this.query.append("MID(").append(this.parent.prepareName(col, true)).append(",").append(start).append(",").append(length).append(") ");
        return this;
    }

    public FunctionBuilder<T> mid(String col, int start)
    {
        this.query.append("MID(").append(this.parent.prepareName(col, true)).append(",").append(start).append(") ");
        return this;
    }

    public FunctionBuilder<T> len(String col)
    {
        this.query.append("LEN(").append(this.parent.prepareName(col, true)).append(") ");
        return this;
    }

    public FunctionBuilder<T> round(String col, int decimals)
    {
        this.query.append("ROUND(").append(this.parent.prepareName(col, true)).append(",").append(decimals).append(") ");
        return this;
    }

    public FunctionBuilder<T> format(String col, String format)
    {
        this.query.append("FORMAT(").append(this.parent.prepareName(col, true)).append(",").append(format).append(") ");
        return this;
    }

    public FunctionBuilder<T> as(String name)
    {
        this.query.append("AS ").append(this.parent.prepareName(name, true)).append(" ");
        return this;
    }

    public FunctionBuilder<T> groupBy(String col)
    {
        this.query.append("GROUP BY ").append(this.parent.prepareName(col, true)).append(" ");
        return this;
    }

    public FunctionBuilder<T> having()
    {
        this.query.append("HAVING ");
        return this;
    }

    public T end()
    {
        this.parent.query.append(this.query);
        return (T)this.parent;
    }
}
