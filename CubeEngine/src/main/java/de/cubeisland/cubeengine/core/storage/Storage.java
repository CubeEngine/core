package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.DatabaseUpdater;
import de.cubeisland.cubeengine.core.util.Callback;
import java.util.Collection;

/**
 *
 * @author Phillip Schichtel
 */
public interface Storage<V extends Model>
{
    /**
     * Returns the model by key
     *
     * @param key the key
     * @return the model
     */
    public V get(Object key);

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
     * Updates the model in the DataBase asynchonous
     *
     * @param model the model to update
     */
    public void update(V model, boolean async);

    /**
     * Merges the model into the DataBase
     *
     * @param model the model to merge in
     */
    public void merge(V model);

    /**
     * Merges the model into the DataBase asynchonous
     *
     * @param model the model to merge in
     */
    public void merge(V model, boolean async);

    /**
     * Deletes the model from DataBase
     *
     * @param model the model to delete
     * @return whether the model got deleted
     */
    public void delete(V model);

    /**
     * Deletes the model from DataBase asynchonous
     *
     * @param model the model to delete
     * @return whether the model got deleted
     */
    public void delete(V model, boolean async);

    /**
     * Deletes the model by ID from DataBase
     *
     * @param id the id to delete
     * @return whether the model got deleted
     */
    public void deleteByKey(Object key);
    /**
     * Deletes the model by ID from DataBase asynchonous
     *
     * @param id the id to delete
     * @return whether the model got deleted
     */
    public void deleteByKey(Object key, boolean async);

    /**
     * Clears the Table
     */
    public void clear();

    /**
     * Subscribes for given SubscribeType
     * 
     * @param type the SubcribeType
     * @param callback the Callback
     */
    public void subscribe(SubcribeType type, Callback callback);

    /**
     * Check if DatabaseStructure needs to be updated and update
     */
    public void updateStructure();

    /**
     * Registers an updater
     * 
     * @param updater the updater
     * @param fromRevision the revision to update from with this updater
     */
    public void registerUpdater(DatabaseUpdater updater, int... fromRevision);

    public enum SubcribeType
    {
        CREATE,
        DELETE,
        UPDATE
    }
}