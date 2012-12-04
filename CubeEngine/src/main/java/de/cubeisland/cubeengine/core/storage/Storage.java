package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.DatabaseUpdater;
import de.cubeisland.cubeengine.core.util.Callback;
import java.util.Collection;

/**
 * This interface provides basic access-methods for accessing the model V with a Key K
 * 
 * @param <K> The Key of the Model M
 * @param <M> The Model of this Storage
 */
public interface Storage<K, M extends Model<K>>
{
    /**
     * Initialize the Storage.
     */
    public void initialize();
    
    /**
     * Returns the model by key
     *
     * @param key the key
     * @return the model
     */
    public M get(K key);

    /**
     * Returns all the models
     *
     * @return the models
     */
    public Collection<M> getAll();

    /**
     * Stores the model into the DataBase
     *
     * @param model the model to store
     */
    public void store(M model);

    /**
     * Stores the model into the DataBase asynchonous
     *
     * @param model the model to store
     */
    public void store(M model, boolean async);

    /**
     * Updates the model in the DataBase
     *
     * @param model the model to update
     */
    public void update(M model);

    /**
     * Updates the model in the DataBase asynchonous
     *
     * @param model the model to update
     */
    public void update(M model, boolean async);

    /**
     * Merges the model into the DataBase
     *
     * @param model the model to merge in
     */
    public void merge(M model);

    /**
     * Merges the model into the DataBase asynchonous
     *
     * @param model the model to merge in
     */
    public void merge(M model, boolean async);

    /**
     * Deletes the model from DataBase
     *
     * @param model the model to delete
     * @return whether the model got deleted
     */
    public void delete(M model);

    /**
     * Deletes the model from DataBase asynchonous
     *
     * @param model the model to delete
     * @return whether the model got deleted
     */
    public void delete(M model, boolean async);

    /**
     * Deletes the model by ID from DataBase
     *
     * @param id the id to delete
     * @return whether the model got deleted
     */
    public void deleteByKey(K key);

    /**
     * Deletes the model by ID from DataBase asynchonous
     *
     * @param id the id to delete
     * @return whether the model got deleted
     */
    public void deleteByKey(K key, boolean async);

    /**
     * Clears the Table
     */
    public void clear();

    /**
     * Subscribes for given SubscribeType
     *
     * @param type     the SubcribeType
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
     * @param updater      the updater
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
