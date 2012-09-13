package de.cubeisland.cubeengine.conomy.account.user;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.UserManager;

/**
 *
 * @author Anselm Brehme
 */
public class UserAccountStorage extends BasicStorage<UserAccount>
{
    private final UserManager cuManager;

    public UserAccountStorage(Database database, UserManager cuManager)
    {
        super(database, UserAccount.class, 1);//TODO
        this.cuManager = cuManager;
    }
}