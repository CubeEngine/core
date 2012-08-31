package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.InsertBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLInsertBuilder extends MySQLComponentBuilder<InsertBuilder> implements InsertBuilder
{
    protected MySQLInsertBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    public MySQLInsertBuilder into(String table)
    {
        Validate.notNull(table, "The table name must not be null!");

        this.query = new StringBuilder("INSERT INTO ").append(this.database.prepareName(table)).append(' ');
        return this;
    }

    public MySQLInsertBuilder cols(String... cols)
    {
        Validate.notEmpty(cols, "You have to specify at least one col to insert");
        Validate.noNullElements(cols, "Column names must not be null!");

        this.query.append('(').append(this.database.prepareFieldName(cols[0]));
        int i;
        for (i = 1; i < cols.length; ++i)
        {
            this.query.append(',').append(this.database.prepareFieldName(cols[i]));
        }
        this.query.append(") VALUES (?");
        for (i = 1; i < cols.length; ++i)
        {
            this.query.append(",?");
        }
        this.query.append(')');
        return this;
    }
}