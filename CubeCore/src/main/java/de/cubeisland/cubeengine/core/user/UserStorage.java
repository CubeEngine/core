package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;

/**
 *
 * @author Phillip Schichtel
 */
public class UserStorage extends BasicStorage<User>
{
    public UserStorage(Database database)
    {
        super(database, User.class);
    }
}