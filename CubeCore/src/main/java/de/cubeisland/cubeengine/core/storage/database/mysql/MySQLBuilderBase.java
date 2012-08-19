package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.QueryBuilder;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class MySQLBuilderBase
{
    protected StringBuilder query;
    protected MySQLQueryBuilder builder;
    
    protected MySQLBuilderBase(MySQLQueryBuilder builder)
    {
        this.query = null;
        this.builder = builder;
    }
    
    public String prepareName(String name)
    {
        return this.builder.database.prepareName(name);
    }
    
    public String prepareColName(String name)
    {
        return this.builder.database.prepareColName(name);
    }

    public QueryBuilder end()
    {
        this.builder.query.append(this.query);
        this.query = null;
        return this.builder;
    }
}
