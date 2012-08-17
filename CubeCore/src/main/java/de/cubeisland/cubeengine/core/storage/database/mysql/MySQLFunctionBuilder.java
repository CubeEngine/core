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
        this.query = new StringBuilder(" WHERE ");
    }
    
    
    public FunctionBuilder<T> currentTime()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> now()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> avg(String col)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> count(String col, boolean distinct)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> count(String col)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> countall()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> min(String col)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> max(String col)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> sum(String col)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> first(String col)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> last(String col)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> ucase(String col)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> lcase(String col)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> mid(String col, int start, int length)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> mid(String col, int start)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> len(String col)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> round(String col, int decimals)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> format(String col, String format)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> as(String name)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> groupBy(String col)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FunctionBuilder<T> having()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public T end()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
