package de.cubeisland.cubeengine.conomy.account.item;

import de.cubeisland.cubeengine.conomy.account.BankAccount;
import de.cubeisland.cubeengine.conomy.account.UserAccount;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.account.storage.AccountStorage;
import de.cubeisland.cubeengine.conomy.currency.Currency;

public class ItemBankAccount extends BankAccount
{
    private AccountStorage storage;

    public ItemBankAccount(String name, Currency currency, AccountModel model, AccountStorage storage)
    {
        super(name, currency, model);
        this.storage = storage;
    }
}
