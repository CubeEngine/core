package de.cubeisland.cubeengine.core.storage.database;

import java.sql.SQLException;

/**
 * Updater that will be automaticly triggered when registered for its revision.
 */
public interface DatabaseUpdater
{
    public void update(Database database) throws SQLException;
}
