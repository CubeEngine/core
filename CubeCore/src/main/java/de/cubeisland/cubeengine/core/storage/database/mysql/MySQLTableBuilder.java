package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.TableBuilder;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLTableBuilder extends MySQLBuilderBase implements TableBuilder
{
    private int fieldCounter;

    protected MySQLTableBuilder(MySQLQueryBuilder builder)
    {
        super(builder);
    }
    
    protected TableBuilder create(String name, int actionIfExists)
    {
        this.fieldCounter = 0;
        name = this.prepareName(name, true);
        switch (actionIfExists)
        {
            case 1: // DO NOTHING
                this.query = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(name).append(" ");
                break;
            case 2: // REPLACE
                this.query = new StringBuilder("DROP TABLE IF EXISTS ").append(name).append(";CREATE TABLE ").append(name);
                break;
            default:
                throw new IllegalArgumentException("Unknown action!");
        }
        return this;
    }

    public TableBuilder beginFields()
    {
        if (this.fieldCounter > 0)
        {
            throw new IllegalStateException("The fields where already specified!");
        }
        this.query.append('(');
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
    
    public TableBuilder field(String name, AttrType type, boolean notnull)
    {
        return this.field(name, type, 0, true);
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
        if (this.fieldCounter > 0)
        {
            this.query.append(',');
        }
        this.query.append(this.prepareName(name, false)).append(' ').append(type.name());
        if (length > 0)
        {
            this.query.append('(').append(length).append(')');
        }
        if (unsigned)
        {
            this.query.append(" UNSIGNED");
        }
        this.query.append(notnull ? " NOT NULL" : " NULL");
        if (ai)
        {
            this.query.append(" AUTO_INCREMENT");
        }
        this.fieldCounter++;

        return this;
    }

    public TableBuilder primaryKey(String key)
    {
        this.query.append(",PRIMARY KEY (").append(this.prepareName(key, false)).append(')');
        return this;
    }

    public TableBuilder foreignKey(String key)
    {
        this.query.append(",FOREIGN KEY (").append(this.prepareName(key, false)).append(')');
        return this;
    }

    public TableBuilder references(String otherTable, String key)
    {
        this.query.append("REFERENCES ").append(this.prepareName(otherTable, true)).append(" (").append(this.prepareName(key, false)).append(')');
        return this;
    }

    public TableBuilder endFields()
    {
        this.query.append(')');
        this.fieldCounter = -1;

        return this;
    }

    public TableBuilder engine(String engine)
    {
        this.query.append("ENGINE=").append(engine).append(" ");
        return this;
    }

    public TableBuilder defaultcharset(String charset)
    {
        this.query.append("DEFAULT CHARSET=").append(charset).append(" ");
        return this;
    }

    public TableBuilder autoIncrement(int n)
    {
        this.query.append("AUTO_INCREMENT=").append(n).append(" ");
        return this;
    }

    @Override
    public QueryBuilder end()
    {
        if (this.fieldCounter >= 0)
        {
            throw new IllegalStateException("A table needs at least one field!");
        }
        return super.end();
    }
}