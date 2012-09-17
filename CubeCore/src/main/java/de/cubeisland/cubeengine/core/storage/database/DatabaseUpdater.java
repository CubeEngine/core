package de.cubeisland.cubeengine.core.storage.database;

import java.sql.SQLException;

/**
 *
 * @author Anselm Brehme
 */
public interface DatabaseUpdater
{
    public void update(Database database) throws SQLException;
}
