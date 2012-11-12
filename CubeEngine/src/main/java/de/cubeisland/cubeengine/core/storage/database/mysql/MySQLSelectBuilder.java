package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import org.apache.commons.lang.Validate;

/**
 * MYSQLQueryBuilder for selecting from tables.
 */
public class MySQLSelectBuilder extends MySQLConditionalBuilder<SelectBuilder> implements SelectBuilder
{
    protected MySQLSelectBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    @Override
    public MySQLSelectBuilder select()
    {
        this.query = new StringBuilder("SELECT ");
        return this;
    }

    @Override
    public MySQLSelectBuilder cols(String... cols)
    {
        if (cols.length > 0)
        {
            this.query.append(this.database.prepareFieldName(cols[0]));
            for (int i = 1; i < cols.length; ++i)
            {
                this.query.append(',').append(this.database.prepareFieldName(cols[i]));
            }
        }
        return this;
    }

    @Override
    public MySQLSelectBuilder from(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified!");
        this.query.append(" FROM ").append(this.database.prepareTableName(tables[0]));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.database.prepareTableName(tables[i]));
        }
        return this;
    }

    @Override
    public MySQLSelectBuilder distinct()
    {
        this.query.append(" DISTINCT");
        return this;
    }

    @Override
    public MySQLSelectBuilder union(boolean all)
    {
        this.query.append(" UNION ");
        return this;
    }

    @Override
    public SelectBuilder into(String table)
    {
        this.query.append(" INTO ").append(this.database.prepareTableName(table));
        return this;
    }

    @Override
    public SelectBuilder in(String database)
    {
        this.query.append(" IN ").append(this.database.prepareString(database));//TODO prepare db name correct?
        return this;
    }
}