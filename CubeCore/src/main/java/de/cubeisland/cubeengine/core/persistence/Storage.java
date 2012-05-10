package de.cubeisland.cubeengine.core.persistence;

import java.util.Collection;

/**
 *
 * @author Phillip Schichtel
 */
public interface Storage<K, V >
//irgendwie mehr Werte reinmachen wäre gut...
{
    public Database getDatabase();
    public Collection<V> getAll();
    public V getByKey(K key);
    public boolean store(V... object);
    public int delete(V... object);
    public int deleteByKey(K... keys);
}
