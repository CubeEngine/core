package de.cubeisland.cubeengine.core.storage;

/**
 *
 * @author Anselm Brehme
 */
public interface LinkingModel<K> extends Model<K>
{
    public <T extends Model> void attach(T model);
    public <T extends Model> T getAttachment(Class<T> modelClass);
}
