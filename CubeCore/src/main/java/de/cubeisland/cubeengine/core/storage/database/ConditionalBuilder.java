package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Phillip Schichtel
 */
public interface ConditionalBuilder
{
    
    public ConditionalBuilder where();
    public ConditionalBuilder orderBy();
    public ConditionalBuilder limit();
    public ConditionalBuilder offset();
    
    public QueryBuilder end();
}
