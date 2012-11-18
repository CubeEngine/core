package de.cubeisland.cubeengine.conomy.account.bank;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;

public class BankAccountStorage extends BasicStorage<BankAccount>
{
    public BankAccountStorage(Database database)
    {
        super(database, BankAccount.class, 1);//TODO
    }
}
