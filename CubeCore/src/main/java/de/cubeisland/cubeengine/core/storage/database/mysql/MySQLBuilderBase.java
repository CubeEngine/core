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
    
    protected MySQLBuilderBase()
    {
        this.query = null;
        this.builder = null;
    }
    
    protected MySQLBuilderBase(MySQLQueryBuilder builder)
    {
        this.query = null;
        this.builder = builder;
    }
    
    public String prepareName(String name, boolean isTableName)
    {
        return this.builder.database.prepareName(name, isTableName);
    }
    
    public String[] prepareNames(String[] names, boolean areTableNames)
    {
        String[] prepared = new String[names.length];
        for (int i = 0 ; i < names.length; ++i)
        {
            prepared[i] = this.prepareName(names[i], areTableNames);
        }
        return prepared;    
    }

    public QueryBuilder end()
    {
        this.builder.query.append(this.query);
        this.query = null;
        return this.builder;
    }
}
