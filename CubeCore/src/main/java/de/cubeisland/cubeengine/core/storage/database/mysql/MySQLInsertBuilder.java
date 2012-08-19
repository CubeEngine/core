package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.InsertBuilder;
import de.cubeisland.cubeengine.core.storage.database.QueryBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
class MySQLInsertBuilder extends MySQLBuilderBase implements InsertBuilder
{
    protected MySQLInsertBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    public InsertBuilder into(String table)
    {
        Validate.notNull(table, "The table name must not be null!");
        
        this.query = new StringBuilder("INSERT INTO ").append(this.prepareName(table, true)).append(' ');
        return this;
    }

    public InsertBuilder cols(String... cols)
    {
        Validate.notEmpty(cols, "You have to specify at least one col to insert");
        Validate.noNullElements(cols, "Column names must not be null!");
        
        this.query.append('(').append(this.prepareName(cols[0], false));
        int i;
        for (i = 1; i < cols.length; ++i)
        {
            this.query.append(',').append(this.prepareName(cols[i], false));
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