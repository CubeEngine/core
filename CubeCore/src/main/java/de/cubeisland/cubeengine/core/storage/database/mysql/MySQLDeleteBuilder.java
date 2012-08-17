package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.DeleteBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLDeleteBuilder extends MySQLConditionalBuilder<DeleteBuilder> implements DeleteBuilder
{
    protected MySQLDeleteBuilder(MySQLQueryBuilder querybuilder)
    {
        super(querybuilder);
    }

    public DeleteBuilder from(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified");
        
        this.query = new StringBuilder("DELETE FROM ").append(this.prepareName(tables[0], true));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.prepareName(tables[i], true));
        }
        return this;
    }
}