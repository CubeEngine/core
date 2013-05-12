package de.cubeisland.cubeengine.conomy.account.exp;

import de.cubeisland.cubeengine.conomy.account.BankAccount;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.account.storage.AccountStorage;
import de.cubeisland.cubeengine.conomy.currency.Currency;

public class ExpBankAccount extends BankAccount
{
    private AccountStorage storage;

    public ExpBankAccount(String name, Currency currency, AccountModel model, AccountStorage storage)
    {
        super(name, currency, model);
        this.storage = storage;
    }
}
