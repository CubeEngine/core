package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLSelectBuilder extends MySQLConditionalBuilder<MySQLSelectBuilder> implements SelectBuilder<MySQLSelectBuilder, MySQLQueryBuilder>
{
    private MySQLQueryBuilder parent;
    
    protected MySQLSelectBuilder(MySQLQueryBuilder parent, Database database)
    {
        super(database);
        this.parent = parent;
    }

    public MySQLSelectBuilder cols(String... cols)
    {
        this.query = new StringBuilder("SELECT ");
        if (cols.length == 0)
        {
            this.query.append('*');
        }
        else
        {
            this.query.append(this.database.prepareFieldName(cols[0]));
            for (int i = 1; i < cols.length; ++i)
            {
                this.query.append(',').append(this.database.prepareFieldName(cols[i]));
            }
        }
        return this;
    }

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

    public MySQLQueryBuilder end()
    {
        this.parent.query.append(this.query.toString());
        this.query = null;
        return this.parent;
    }
}