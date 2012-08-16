package de.cubeisland.cubeengine.core.persistence;

import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.util.StringUtils;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLBuilder implements QueryBuilder
{
    private StringBuilder query;
    private StringBuilder queryend;
    private Database database;

    public MySQLBuilder(Database database)
    {
        this.query = new StringBuilder();
        this.queryend = new StringBuilder();
        this.database = database;
    }

    public QueryBuilder select(String... col)
    {
        query.append(" SELECT ").append(StringUtils.implode(",", col));
        return this;
    }

    public QueryBuilder from(String table)
    {
        query.append(" FROM ").append(this.quote(table));
        return this;
    }

    public QueryBuilder delete()
    {
        query.append(" DELETE");
        return this;
    }

    public QueryBuilder insertInto(String table, String... col)
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
        query.append(" ORDER BY ").append(col);
        return this;
    }

    public QueryBuilder offset(int n)
    {
        query.append(" OFFSET ").append(n);
        return this;
    }

    public QueryBuilder where(String... conditions)
    {
        query.append(" WHERE ").append(StringUtils.implode("=? AND ", conditions)).append("=?");
        return this;
    }

    public QueryBuilder limit(int n)
    {
        query.append(" LIMIT ").append(n);
        return this;
    }

    public QueryBuilder createTable(TableBuilder tb, boolean ifNoExist)
    {
        query.append(" CREATE TABLE");
        if (ifNoExist)
        {
            query.append(" IF NOT EXISTS ");
        }
        query.append(tb.toString());
        return this;
    }

    

    public QueryBuilder engine(String engine)
    {
        queryend.append(" ENGINE=").append(engine);
        return this;
    }

    public QueryBuilder defaultcharset(String charset)
    {
        queryend.append(" DEFAULT CHARSET=").append(charset);
        return this;
    }

    public QueryBuilder autoincrement(int n)
    {
        queryend.append(" AUTO_INCREMENT=").append(n);
        return this;
    }

    private String quote(String s)
    {
        return this.database.quote(s);
    }

    public String toString()
    {
        return query.toString() + queryend.toString();
    }

}
