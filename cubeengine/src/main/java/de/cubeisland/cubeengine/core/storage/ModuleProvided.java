package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.module.Module;

/**
 * Interface mainly for DatabaseModels
 * <p>make sure the storage-manager implements the @linkModuleProvider-Interface</p>
 * <p>the module will then be set automatically upon creation
 * @param <M>
 */
public interface ModuleProvided<M extends Module>
{
    void setModule(M module);
}
