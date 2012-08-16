package de.cubeisland.cubeengine.core.persistence.database.mysql;

import de.cubeisland.cubeengine.core.persistence.database.QueryBuilder;
import de.cubeisland.cubeengine.core.persistence.database.TableBuilder;
import de.cubeisland.cubeengine.core.persistence.database.mysql.MySQLDatabase;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLBuilder implements QueryBuilder
{
    protected StringBuilder query;
    protected MySQLDatabase database;

    public MySQLBuilder(MySQLDatabase database)
    {
        this.query = null;
        this.database = database;
    }

    public QueryBuilder select(String... cols)
    {
        this.query = new StringBuilder("SELECT ");
        if (cols.length == 0)
        {
            this.query.append('*');
        }
        else
        {
            this.query.append(this.database.quote(cols[0]));
            if (cols.length > 1)
            {
                for (int i = 0; i < cols.length; ++i)
                {
                    this.query.append(',').append(this.database.quote(cols[i]));
                }
            }
        }
        
        return this;
    }

    public QueryBuilder from(String... tables)
    {
        Validate.isTrue(tables.length > 0, "You need to specify at least one table!");
        
        this.query.append(" FROM ").append(this.database.quote(tables[0]));
        if (tables.length > 1)
        {
            for (int i = 0; i < tables.length; ++i)
            {
                this.query.append(',').append(this.database.quote(tables[i]));
            }
        }
        return this;
    }

    public QueryBuilder delete()
    {
        query.append("DELETE");
        return this;
    }

    public QueryBuilder insertInto(String table, String... col) // TODO multitable support
    {
        query.append(" INSERT INTO ").append(this.database.quote(table)).append(" (").append(StringUtils.implode(",", col)).append(")");
        return this;
    }

    public QueryBuilder values(int n)
    {
        if (n < 1)
        {
            throw new IllegalStateException("Need at least 1 value");
        }
        query.append(" VALUES ").append("?").append(StringUtils.repeat(",?", n - 1));
        return this;
    }

    public QueryBuilder orderBy(String col)
    {
        query.append(" ORDER BY ").append(this.database.quote(col));
        return this;
    }

    public QueryBuilder offset(int n)
    {
        query.append(" OFFSET ").append(n);
        return this;
    }

    public QueryBuilder where(String... conditions)
    {
        query.append(" WHERE ").append(StringUtils.implode("=? AND ", conditions)).append("=?"); // TODO what if we want to link via OR ?
        return this;
    }

    public QueryBuilder limit(int n)
    {
        query.append(" LIMIT ").append(n);
        return this;
    }

    public TableBuilder createTable(String name, boolean ifNoExist)
    {
        return new MySQLTableBuilder(this, name, ifNoExist ? 1 : 2);
    }

    @Override
    public String end()
    {
        return this.query.toString();
    }
    
    public QueryBuilder clear()
    {
        this.query = null;
        
        return this;
    }
}
