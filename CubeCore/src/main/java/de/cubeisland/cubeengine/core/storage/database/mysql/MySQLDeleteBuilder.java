package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.DeleteBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLDeleteBuilder extends MySQLComponentBuilder<MySQLDeleteBuilder,MySQLQueryBuilder> implements DeleteBuilder<MySQLDeleteBuilder,MySQLQueryBuilder>
{
    private MySQLQueryBuilder parent;
    
    protected MySQLDeleteBuilder(MySQLQueryBuilder parent, Database database)
    {
        super(database);
        this.parent = parent;
    }

    public MySQLDeleteBuilder from(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified");
        
        this.query = new StringBuilder("DELETE FROM ").append(this.database.prepareFieldName(tables[0]));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.database.prepareFieldName(tables[i]));
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