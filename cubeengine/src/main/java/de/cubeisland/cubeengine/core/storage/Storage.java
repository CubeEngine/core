/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.core.storage;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import de.cubeisland.cubeengine.core.storage.database.DatabaseUpdater;

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
     * Stores the model into the DataBase asynchronous
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
     * Updates the model in the DataBase asynchronous
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
     * Merges the model into the DataBase asynchronous
     *
     * @param model the model to merge in
     */
    public void merge(M model, boolean async);

    /**
     * Deletes the model from DataBase
     *
     * @param model the model to delete
     */
    public void delete(M model);

    /**
     * Deletes the model from DataBase asynchronously
     *
     * @param model the model to delete
     */
    public void delete(M model, boolean async);

    /**
     * Deletes the model by ID from DataBase
     *
     * @param key the id to delete
     */
    public void deleteByKey(K key);

    /**
     * Deletes the model by ID from DataBase asynchronously
     *
     * @param key the id to delete
     */
    public void deleteByKey(K key, boolean async);

    /**
     * Clears the Table
     */
    public void clear();

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

    /**
     * Creates a model for the resultSet values
     *
     * @param resultSet the ResultSet to get the data from
     * @return a new model with data
     */
    public M createModel(ResultSet resultSet) throws IllegalAccessException, InstantiationException, SQLException, InvocationTargetException;

    public enum SubscribeType
    {
        CREATE,
        DELETE,
        UPDATE
    }
}
