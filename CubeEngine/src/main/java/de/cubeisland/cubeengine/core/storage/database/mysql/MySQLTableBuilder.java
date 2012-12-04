package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.TableBuilder;

/**
 * MYSQLQueryBuilder for creating new tables.
 */
public class MySQLTableBuilder extends MySQLComponentBuilder<TableBuilder>
        implements TableBuilder
{
    private int fieldCounter;

    protected MySQLTableBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    protected MySQLTableBuilder create(String name, int actionIfExists)
    {
        this.fieldCounter = 0;
        name = this.database.prepareTableName(name);
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

    @Override
    public MySQLTableBuilder beginFields()
    {
        if (this.fieldCounter > 0)
        {
            throw new IllegalStateException("The fields where already specified!");
        }
        this.query.append('(');
        return this;
    }

    @Override
    public MySQLTableBuilder field(String name, AttrType type)
    {
        return this.field(name, type, 0);
    }

    @Override
    public MySQLTableBuilder field(String name, AttrType type, int length)
    {
        return this.field(name, type, length, true);
    }

    @Override
    public MySQLTableBuilder field(String name, AttrType type, boolean notnull)
    {
        return this.field(name, type, 0, true);
    }

    @Override
    public MySQLTableBuilder field(String name, AttrType type, int length, boolean notnull)
    {
        return this.field(name, type, length, notnull, false);
    }

    @Override
    public MySQLTableBuilder field(String name, AttrType type, int length, boolean notnull, boolean unsigned)
    {
        return this.field(name, type, length, notnull, unsigned, false);
    }

    @Override
    public MySQLTableBuilder field(String name, AttrType type, int length, boolean notnull, boolean unsigned, boolean ai)
    {
        if (this.fieldCounter > 0)
        {
            this.query.append(',');
        }
        this.query.append(this.database.prepareFieldName(name)).append(' ').append(type.name());
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

    @Override
    public MySQLTableBuilder primaryKey(String... keys)
    {
        if (keys.length == 1)
        {
            this.query.append(",PRIMARY KEY (").append(this.database.prepareFieldName(keys[0])).append(')');
        }
        else
        {
            this.query.append(",PRIMARY KEY (").append(this.database.prepareFieldName(keys[0]));
            for (int i = 1; i < keys.length; ++i)
            {
                this.query.append(", ").append(this.database.prepareFieldName(keys[i]));
            }
            this.query.append(")");
        }
        return this;
    }

    @Override
    public MySQLTableBuilder foreignKey(String key)
    {
        this.query.append(",FOREIGN KEY (").append(this.database.prepareFieldName(key)).append(')');
        return this;
    }

    @Override
    public MySQLTableBuilder references(String otherTable, String field)
    {
        this.query.append(" REFERENCES ").append(this.database.prepareTableName(otherTable)).append(" (").append(this.database.prepareFieldName(field)).append(')');
        return this;
    }

    @Override
    public TableBuilder onDelete(String doThis)
    {
        this.query.append(" ON DELETE ").append(doThis);
        return this;
    }

    @Override
    public MySQLTableBuilder endFields()
    {
        this.query.append(')');
        this.fieldCounter = -1;

        return this;
    }

    @Override
    public MySQLTableBuilder engine(String engine)
    {
        this.query.append("ENGINE=").append(engine).append(" ");
        return this;
    }

    @Override
    public MySQLTableBuilder defaultcharset(String charset)
    {
        this.query.append("DEFAULT CHARSET=").append(charset).append(" ");
        return this;
    }

    @Override
    public MySQLTableBuilder autoIncrement(int n)
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

    @Override
    public TableBuilder unique(String field)
    {
        this.query.append(",UNIQUE(").append(this.database.prepareFieldName(field)).append(")");
        return this;
    }

    @Override
    public TableBuilder check()
    {
        this.query.append(" CHECK ");
        return this;
    }
}
