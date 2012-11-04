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
    public MySQLSelectBuilder cols(String... cols)
    {
        this.query = new StringBuilder("SELECT ");
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

        this.query.append(" FROM ").append(this.database.prepareName(tables[0]));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.database.prepareName(tables[i]));
        }
        return this;
    }
}