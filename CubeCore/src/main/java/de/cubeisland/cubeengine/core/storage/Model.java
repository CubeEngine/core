package de.cubeisland.cubeengine.core.storage;

/**
 *
 * @author Anselm Brehme
 */
public interface Model<K>
{
    /**
     * Returns the key of this model 
     * 
     * @return the key
     */
    public K getKey();
    /**
     * Sets the key of this model.
     * 
     * @param key the key
     */
    public void setKey(K key);
}