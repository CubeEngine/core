package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.TableBuilder;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLTableBuilder implements TableBuilder
{
    private MySQLQueryBuilder builder;
    private StringBuilder query;
    private MySQLDatabase database;
    private int fieldCounter;

    public MySQLTableBuilder(MySQLQueryBuilder builder, String tablename, int actionIfExists)
    {
        this.builder = builder;
        this.database = builder.database;
        tablename = this.database.quote(tablename);
        switch (actionIfExists)
        {
            case 1: // DO NOTHING
                this.query = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(tablename);
                break;
            case 2: // REPLACE
                this.query = new StringBuilder("DROP TABLE IF EXISTS ").append(tablename).append(";CREATE TABLE ").append(tablename);
                break;
            default:
                throw new IllegalArgumentException("Unknown action!");
        }

        this.fieldCounter = 0;
    }

    public TableBuilder startFields()
    {
        this.query.append("(");

        return this;
    }

    public TableBuilder field(String name, AttrType type)
    {
        return this.field(name, type, 0);
    }

    public TableBuilder field(String name, AttrType type, int length)
    {
        return this.field(name, type, length, true);
    }

    public TableBuilder field(String name, AttrType type, int length, boolean notnull)
    {
        return this.field(name, type, length, notnull, false);
    }

    public TableBuilder field(String name, AttrType type, int length, boolean notnull, boolean unsigned)
    {
        return this.field(name, type, length, notnull, unsigned, false);
    }

    public TableBuilder field(String name, AttrType type, int length, boolean notnull, boolean unsigned, boolean ai)
    {
        if (this.fieldCounter > this.fieldCounter)
        {
            this.query.append(",");
        }
        this.query.append(this.database.quote(name)).append(" ").append(type.name());
        if (length > 0)
        {
            this.query.append("(").append(length).append(")");
        }
        this.query.append(notnull ? "NOT NULL" : "NULL");
        if (unsigned)
        {
            this.query.append(" UNSIGNED");
        }
        if (ai)
        {
            this.query.append(" AUTO_INCREMENT");
        }
        this.fieldCounter++;

        return this;
    }

    public TableBuilder primaryKey(String key)
    {
        this.query.append("PRIMARY KEY (").append(this.database.quote(key)).append(")");
        return this;
    }

    public TableBuilder foreignKey(String key)
    {
        this.query.append("FOREIGN KEY (").append(this.database.quote(key)).append(")");
        return this;
    }

    public TableBuilder references(String otherTable, String key)
    {
        this.query.append("REFERENCES ").append(this.database.quote(otherTable)).append("(").append(this.database.quote(key)).append(")");
        return this;
    }

    public TableBuilder endFields()
    {
        this.query.append(")");
        this.fieldCounter = -1;

        return this;
    }

    public TableBuilder engine(String engine)
    {
        this.query.append(" ENGINE=").append(engine);
        return this;
    }

    public TableBuilder defaultcharset(String charset)
    {
        this.query.append(" DEFAULT CHARSET=").append(charset);
        return this;
    }

    public TableBuilder autoIncrement(int n)
    {
        this.query.append(" AUTO_INCREMENT=").append(n);
        return this;
    }

    public QueryBuilder endCreateTable()
    {
        if (this.fieldCounter >= 0)
        {
            throw new IllegalStateException("A table needs at least one field!");
        }
        this.builder.query.append(query).append(";");

        this.database = null;
        this.query = null;

        return this.builder;
    }
}