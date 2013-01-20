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
    private int foreignKeys;

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
        return this.field(name, type, false, length, notnull);
    }

    @Override
    public MySQLTableBuilder field(String name, AttrType type, boolean unsigned, boolean notnull)
    {
        return this.field(name, type, unsigned, 0, notnull);
    }

    @Override
    public MySQLTableBuilder field(String name, AttrType type, boolean unsigned, int length, boolean notnull)
    {
        if (this.fieldCounter > 0)
        {
            this.query.append(",\n");
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
        this.fieldCounter++;

        return this;
    }

    @Override
    public MySQLTableBuilder enumField(String name, String[] enumValues, boolean notnull)
    {
        if (this.fieldCounter > 0)
        {
            this.query.append(',');
        }
        if (enumValues.length == 0)
        {
            throw new IllegalStateException("Enum cannot be empty!");
        }
        this.query.append(this.database.prepareFieldName(name)).append(' ').append(AttrType.ENUM.name()).append("(").append(this.database.prepareString(enumValues[0]));
        for (int i = 1; i < enumValues.length; ++i)
        {
            this.query.append(", ").append(this.database.prepareString(enumValues[i]));
        }
        this.query.append(notnull ? " NOT NULL" : " NULL");
        this.fieldCounter++;
        return this;
    }

    @Override
    public MySQLTableBuilder defaultValue(String sql)
    {
        this.query.append(" DEFAULT ").append(sql);
        return this;

    }

    @Override
    public MySQLTableBuilder autoIncrement()
    {
        this.query.append(" AUTO_INCREMENT");
        return this;
    }

    @Override
    public MySQLTableBuilder index(String... fields)
    {
        if (fields.length == 0)
        {
            throw new IllegalArgumentException("Foreign Keys can not be none!");
        }
        this.query.append(", INDEX (").append(this.database.prepareFieldName(fields[0]));
        for (int i = 1; i < fields.length; ++i)
        {
            this.query.append(", ").append(this.database.prepareFieldName(fields[i]));
        }
        this.query.append(')');
        return this;
    }

    @Override
    public MySQLTableBuilder primaryKey(String... fields)
    {
        if (fields.length == 1)
        {
            this.query.append(",PRIMARY KEY (").append(this.database.prepareFieldName(fields[0])).append(')');
        }
        else
        {
            this.query.append(",PRIMARY KEY (").append(this.database.prepareFieldName(fields[0]));
            for (int i = 1; i < fields.length; ++i)
            {
                this.query.append(", ").append(this.database.prepareFieldName(fields[i]));
            }
            this.query.append(")");
        }
        return this;
    }

    @Override
    public MySQLTableBuilder foreignKey(String... keys)
    {
        if (keys.length == 0)
        {
            throw new IllegalArgumentException("Foreign Keys can not be none!");
        }
        this.query.append(", FOREIGN KEY (").append(this.database.prepareFieldName(keys[0]));
        for (int i = 1; i < keys.length; ++i)
        {
            this.query.append(", ").append(this.database.prepareFieldName(keys[i]));
        }
        this.query.append(')');
        this.foreignKeys = keys.length;
        return this;
    }

    @Override
    public MySQLTableBuilder references(String otherTable, String... fields)
    {
        if (fields.length == 0)
        {
            throw new IllegalArgumentException("Foreign Key-references can not be none!");
        }
        if (this.foreignKeys != fields.length)
        {
            throw new IllegalArgumentException("Foreign Key-references have to be the same amount as the foreign keys!");
        }
        this.query.append(" REFERENCES ").
                append(this.database.prepareTableName(otherTable)).
                append(" (").append(this.database.prepareFieldName(fields[0]));
        for (int i = 1; i < fields.length; ++i)
        {
            this.query.append(", ").append(this.database.prepareFieldName(fields[i]));
        }
        this.query.append(')');
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
    public MySQLTableBuilder unique(String... fields)
    {
        this.query.append(", UNIQUE(").append(this.database.prepareFieldName(fields[0]));
        for (int i = 1; i < fields.length; ++i)
        {
            this.query.append(", ").append(this.database.prepareFieldName(fields[i]));
        }
        this.query.append(")");
        return this;
    }

    @Override
    public MySQLTableBuilder check()
    {
        this.query.append(" CHECK ");
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
