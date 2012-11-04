package de.cubeisland.cubeengine.core.storage.database;

import de.cubeisland.cubeengine.core.config.Configuration;

/**
 * DatabaseConfiguration have to return their corresponding DatabaseClass.
 */
public abstract class DatabaseConfiguration extends Configuration
{
    public abstract Class<? extends Database> getDatabaseClass();
}
