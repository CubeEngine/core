package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.InsertBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLInsertBuilder extends MySQLComponentBuilder<MySQLInsertBuilder,MySQLQueryBuilder> implements InsertBuilder<MySQLInsertBuilder,MySQLQueryBuilder>
{
    private MySQLQueryBuilder parent;
    
    protected MySQLInsertBuilder(MySQLQueryBuilder parent, Database database)
    {
        super(database);
        this.parent = parent;
    }
    
    protected MySQLInsertBuilder(Database database)
    {
        super(database);
    }

    public MySQLInsertBuilder into(String table)
    {
        Validate.notNull(table, "The table name must not be null!");
        
        this.query = new StringBuilder("INSERT INTO ").append(this.database.prepareFieldName(table)).append(' ');
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
        for (i = 0; i < cols.length; ++i)
        {
            this.query.append(",?");
        }
        this.query.append(')');
        return this;
    }

    public MySQLQueryBuilder end()
    {
        this.parent.query.append(this.query.toString());
        return this.parent;
    }
}