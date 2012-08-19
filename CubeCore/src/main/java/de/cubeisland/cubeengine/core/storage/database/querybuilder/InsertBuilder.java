package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface InsertBuilder
{
    public InsertBuilder into(String table);
    public InsertBuilder cols(String... cols);
    
    public QueryBuilder end();
}
