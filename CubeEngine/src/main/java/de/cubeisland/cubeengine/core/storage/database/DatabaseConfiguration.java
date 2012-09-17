package de.cubeisland.cubeengine.core.storage.database;

import de.cubeisland.cubeengine.core.config.Configuration;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class DatabaseConfiguration extends Configuration
{
    public abstract Class<? extends Database> getDatabaseClass();
}
