package de.cubeisland.cubeengine.core.storage;

//TODO DOCU
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
     *
     *
     * @param <T>
     * @param modelClass
     * @return
     */
    public <T extends Model> T getAttachment(Class<T> modelClass);
}
