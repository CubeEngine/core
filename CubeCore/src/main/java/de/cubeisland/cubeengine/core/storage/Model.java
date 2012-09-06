package de.cubeisland.cubeengine.core.storage;

/**
 *
 * @author Anselm Brehme
 */
public interface Model<K>
{
    public K getKey();
    public void setKey(K key);
}