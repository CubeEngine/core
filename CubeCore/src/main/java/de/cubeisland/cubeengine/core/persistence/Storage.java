package de.cubeisland.cubeengine.core.persistence;

import java.util.Collection;

/**
 *
 * @author Phillip Schichtel
 */
public interface Storage<V extends Model>
{
    public void initialize();
    public V get(int key);
    //TODO public int get(V model) wenn der key noch nicht festgelegt war muss ich den zugewiesenen key in der Datenbank bekommen
    public Collection<V> getAll();

    public void store(V model);
    public void update(V model);
    public void merge(V model);

    public boolean delete(V model);
    public boolean delete(int id);
    public void clear();
}
