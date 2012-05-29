package de.cubeisland.cubeengine.core.persistence;

/**
 * Storage objects have to implement this
 *
 * @author Phillip Schichtel
 */
public interface Model<T>
{
    /**
     * Returns the ID of this Model
     * 
     * @return the ID
     */
    public T getKey();
    
    /**
     * Sets the ID of this Model
     * DO NOT USE except you REALLY want to change the ID of the Model but not in the DataBase
     * 
     */
    public void setKey(T key);
}
