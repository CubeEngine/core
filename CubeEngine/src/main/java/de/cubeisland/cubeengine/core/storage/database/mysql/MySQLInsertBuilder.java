package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.InsertBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import org.apache.commons.lang.Validate;

/**
 * MYSQLQueryBuilder for inserting into tables.
 */
public class MySQLInsertBuilder extends MySQLComponentBuilder<InsertBuilder> implements InsertBuilder
{
    protected MySQLInsertBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    private boolean colsSet;
    private boolean valuesSet;
    
    @Override
    public MySQLInsertBuilder into(String table)
    {
        Validate.notNull(table, "The table name must not be null!");

        this.query = new StringBuilder("INSERT INTO ").append(this.database.prepareTableName(table)).append(' ');
        return this;
    }

    @Override
    public MySQLInsertBuilder cols(String... cols)
    {
        Validate.notEmpty(cols, "You have to specify at least one col to insert");
        Validate.noNullElements(cols, "Column names must not be null!");

        this.query.append('(').append(this.database.prepareFieldName(cols[0]));
        for (int i = 1; i < cols.length; ++i)
        {
            this.query.append(',').append(this.database.prepareFieldName(cols[i]));
        }
        this.query.append(")"); 
        this.colsSet = true;
        return this.values(cols.length);
    }
    
    @Override
    public MySQLInsertBuilder allCols()
    {
        this.colsSet = true;
        return this;
    }

    @Override
    public MySQLInsertBuilder values(int amount)
    {
        if (amount <= 0)
        {
            throw new IllegalStateException("Cannot add less than one value!");
        }
        super.values(amount);
        this.valuesSet = true;
        return this;
    }

    @Override
    public QueryBuilder end()
    {
        if (!(this.colsSet && this.valuesSet))
        {
            throw new IllegalStateException("Cols and/or amount of values not defined!");
        }
        this.colsSet = false;
        this.valuesSet = false;
        return super.end();
    }
}