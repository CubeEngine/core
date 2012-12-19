package de.cubeisland.cubeengine.core.storage;

/**
 * A Model to save into the database with keytype K
 * that can be linked to other models.
 *
 * @param <K>
 */
public interface LinkingModel<K> extends Model<K>
{
    /**
     * Attaches a model T to this model
     *
     * @param <T>   the models Class
     * @param model the model to attach
     */
    public <T extends Model> void attach(T model);

    /**
     * Returns the attachment
     *
     * @param <T> the type of the attachment
     * @param modelClass the attachment class
     * @return the attachment
     */
    public <T extends Model> T getAttachment(Class<T> modelClass);
}
