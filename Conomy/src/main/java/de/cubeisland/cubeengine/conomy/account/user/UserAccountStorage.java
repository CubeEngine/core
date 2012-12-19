package de.cubeisland.cubeengine.conomy.account.user;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.UserManager;

public class UserAccountStorage extends SingleKeyStorage<Long, UserAccount>
{
    private final UserManager cuManager;

    public UserAccountStorage(Database database, UserManager cuManager)
    {
        super(database, UserAccount.class, 1);//TODO
        this.cuManager = cuManager;
    }
}
