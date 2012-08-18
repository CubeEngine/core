package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Phillip Schichtel
 */
public interface WhereBuilder<T> extends CompareBuilder<WhereBuilder>
{
    
    
    public T endWhere();
}
