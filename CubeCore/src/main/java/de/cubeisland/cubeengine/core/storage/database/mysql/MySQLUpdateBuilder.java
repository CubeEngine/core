package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.UpdateBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLUpdateBuilder extends MySQLConditionalBuilder<UpdateBuilder> implements UpdateBuilder
{
    private boolean hasCols;

    protected MySQLUpdateBuilder(MySQLQueryBuilder builder)
    {
        super(builder);
    }

    public UpdateBuilder tables(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified!");
        
        this.hasCols = false;
        this.query = this.query.append(this.prepareName(tables[0]));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.prepareName(tables[i]));
        }
        return this;
    }

    public UpdateBuilder cols(String... cols)
    {
        Validate.notEmpty(cols, "No cols specified!");
        
        this.query.append(" SET ").append(this.prepareColName(cols[0])).append("=? ");
        for (int i = 1; i < cols.length; ++i)
        {
            this.query.append(',').append(this.prepareColName(cols[i])).append("=? ");
        }
        
        this.hasCols = true;
        
        return this;
    }

    @Override
    public QueryBuilder end()
    {
        if (!this.hasCols)
        {
            throw new IllegalStateException("No cols where specified!");
        }
        return super.end();
    }
}