package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.DeleteBuilder;
import org.apache.commons.lang.Validate;

/**
 * MYSQLQueryBuilder for deleting tables.
 */
public class MySQLDeleteBuilder extends MySQLConditionalBuilder<DeleteBuilder>
    implements DeleteBuilder
{
    protected MySQLDeleteBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    @Override
    public MySQLDeleteBuilder from(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified");

        this.query = new StringBuilder("DELETE FROM ").append(this.database.prepareTableName(tables[0]));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.database.prepareFieldName(tables[i]));
        }
        return this;
    }
}
