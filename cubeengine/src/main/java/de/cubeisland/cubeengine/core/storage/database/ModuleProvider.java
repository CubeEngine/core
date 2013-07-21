package de.cubeisland.cubeengine.core.storage.database;

import de.cubeisland.cubeengine.core.module.Module;

/**
 * Provides access to the module
 *
 * @param <M> the exact moduleType (may be omitted)
 */
public interface ModuleProvider<M extends Module>
{
    M getModule();
}
