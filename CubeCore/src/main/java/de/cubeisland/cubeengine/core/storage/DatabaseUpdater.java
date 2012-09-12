package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.Database;

/**
 *
 * @author Anselm Brehme
 */
public interface DatabaseUpdater
{
    public <T extends Database> void update(T database);
}
