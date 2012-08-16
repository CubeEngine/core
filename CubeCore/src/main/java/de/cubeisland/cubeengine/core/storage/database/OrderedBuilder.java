package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Phillip Schichtel
 */
public interface OrderedBuilder<T> extends ConditionalBuilder<T>
{   
    public T orderBy();
    public T limit();
    public T offset();
    
    public QueryBuilder end();
}
