package de.cubeisland.cubeengine.core.persistence;

/**
 * Storage objects have to implement this
 *
 * @author Phillip Schichtel
 */
public interface Model
{
    /**
     * Returns the ID of this Model
     * 
     * @return the ID
     */
    public int getId();
}
