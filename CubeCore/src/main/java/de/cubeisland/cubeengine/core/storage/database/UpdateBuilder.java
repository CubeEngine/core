package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Phillip Schichtel
 */
public interface UpdateBuilder extends ConditionalBuilder
{
    public UpdateBuilder tables(String... tables);
    
    public UpdateBuilder beginSets();
    
    public UpdateBuilder set(String col); // no value, because we're using prepared statements only!
    
    public UpdateBuilder endSets();
}
