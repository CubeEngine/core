package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.storage.database.Database;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class DatabaseConfiguration extends Configuration
{
    public abstract Class<? extends Database> getDatabaseClass();
}
