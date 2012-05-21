package de.cubeisland.cubeengine.core.persistence;

import java.sql.PreparedStatement;
import java.util.Collection;

/**
 *
 * @author Phillip Schichtel
 */
public interface Storage<V extends Model>
{
    /**
     * Initializes the DataBase
     */
    public void initialize();
    /**
     * Returns the model by key
     * 
     * @param key the key
     * @return the model
     */
    public V get(int key);
    /**
     * Returns all the models
     * 
     * @return the models
     */
    public Collection<V> getAll();

    /**
     * Stores the model into the DataBase
     * 
     * @param model the model to store
     */
    public void store(V model);
    /**
     * Updates the model in the DataBase
     * 
     * @param model the model to update
     */
    public void update(V model);
    /**
     * Merges the model into the DataBase
     * 
     * @param model the model to merge in
     */
    public void merge(V model);

    /**
     * Deletes the model from DataBase
     * 
     * @param model the model to delete
     * @return whether the model got deleted
     */
    public boolean delete(V model);
    /**
     * Deletes the model by ID from DataBase
     * 
     * @param id the id to delete
     * @return whether the model got deleted
     */
    public boolean delete(int id);
    /**
     * Clears the Table
     */
    public void clear();
}
