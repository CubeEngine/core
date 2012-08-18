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
        if (cols == null)
        {
            //Nothing after SELECT so i can use functions etc
        }
        else if (cols.length == 0)
        {
            this.query.append('*');
        }
        else
        {
            this.query.append(this.prepareName(cols[0], false));
            for (int i = 1; i < cols.length; ++i)
            {
                this.query.append(',').append(this.prepareName(cols[i], false));
            }
        }
        return this;
        //TODO Select with Functions
    }

    public SelectBuilder from(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified!");

        this.query.append(" FROM ").append(this.prepareName(tables[0], true));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.prepareName(tables[i], true));
        }
        return this;
    }
}