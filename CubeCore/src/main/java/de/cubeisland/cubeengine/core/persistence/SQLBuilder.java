package de.cubeisland.cubeengine.core.persistence;

import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.util.StringUtils;

/**
 *
 * @author Anselm Brehme
 */
public class SQLBuilder implements QuerryBuilder
{
    private StringBuilder query;
    private StringBuilder queryend;
    private Database database;

    public SQLBuilder(Database database)
    {
        this.query = new StringBuilder();
        this.queryend = new StringBuilder();
        this.database = database;
    }

    public QuerryBuilder select(String... col)
    {
        query.append(" SELECT ").append(StringUtils.implode(",", col));
        return this;
    }

    public QuerryBuilder from(String table)
    {
        query.append(" FROM ").append(this.quote(table));
        return this;
    }

    public QuerryBuilder delete()
    {
        query.append(" DELETE");
        return this;
    }

    public QuerryBuilder insertinto(String table, String... col)
    {
        query.append(" INSERT INTO ").append(this.database.quote(table)).append(" (").append(StringUtils.implode(",", col)).append(")");
        return this;
    }

    public QuerryBuilder values(int n)
    {
        if (n < 1)
        {
            throw new IllegalStateException("Need at least 1 value");
        }
        query.append(" VALUES ").append("?").append(StringUtils.repeat(",?", n - 1));
        return this;
    }

    public QuerryBuilder orderBy(String col)
    {
        query.append(" ORDER BY ").append(col);
        return this;
    }

    public QuerryBuilder offset(int n)
    {
        query.append(" OFFSET ").append(n);
        return this;
    }

    public QuerryBuilder where(String... conditions)
    {
        query.append(" WHERE ").append(StringUtils.implode("=? AND ", conditions)).append("=?");
        return this;
    }

    public QuerryBuilder limit(int n)
    {
        query.append(" LIMIT ").append(n);
        return this;
    }

    public QuerryBuilder createTableINE(String table)
    {
        query.append(" CREATE TABLE IF NOT EXISTS ").append(this.quote(table)).append("( ");
        queryend.append(")");
        return this;
    }

    public QuerryBuilder attribute(String name, AttrType type)
    {
        query.append(this.quote(name)).append(" ").append(type.name());
        return this;
    }

    public QuerryBuilder attribute(String name, AttrType type, int length)
    {
        query.append(this.quote(name)).append(" ").append(type.name()).append("(").append(length).append(")");
        return this;
    }

    public QuerryBuilder next()
    {
        query.append(",");
        return this;
    }

    public QuerryBuilder primaryKey(String key)
    {
        query.append("PRIMARY KEY (").append(this.quote(key)).append(")");
        return this;
    }

    public QuerryBuilder foreignKey(String key)
    {
        query.append("FOREIGN KEY (").append(this.quote(key)).append(")");
        return this;
    }

    public QuerryBuilder references(String table, String key)
    {
        query.append("REFERENCES ").append(this.quote(table)).append("(").append(this.quote(key)).append(")");
        return this;
    }

    public QuerryBuilder unsigned()
    {
        query.append(" unsigned");
        return this;
    }
    
    public QuerryBuilder notNull()
    {
        query.append(" NOT NULL");
        return this;
    }

    public QuerryBuilder nulL()
    {
        query.append(" NULL");
        return this;
    }

    public QuerryBuilder autoincrement()
    {
        query.append(" AUTO_INCREMENT");
        return this;
    }

    public QuerryBuilder engine(String engine)
    {
        queryend.append(" ENGINE=").append(engine);
        return this;
    }

    public QuerryBuilder defaultcharset(String charset)
    {
        queryend.append(" DEFAULT CHARSET=").append(charset);
        return this;
    }

    public QuerryBuilder autoincrement(int n)
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
