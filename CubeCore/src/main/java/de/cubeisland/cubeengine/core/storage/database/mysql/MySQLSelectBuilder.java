package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.SelectBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLSelectBuilder extends MySQLConditionalBuilder<SelectBuilder> implements SelectBuilder
{
    protected MySQLSelectBuilder(MySQLQueryBuilder builder)
    {
        super(builder);
    }

    public SelectBuilder cols(String... cols)
    {
        this.query = new StringBuilder("SELECT ");
        if (cols.length == 0)
        {
            this.query.append('*');
        }
        else
        {
            this.query.append(this.prepareColName(cols[0]));
            for (int i = 1; i < cols.length; ++i)
            {
                this.query.append(',').append(this.prepareColName(cols[i]));
            }
        }
        return this;
    }

    public SelectBuilder from(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified!");

        this.query.append(" FROM ").append(this.prepareName(tables[0]));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.prepareName(tables[i]));
        }
        return this;
    }
}