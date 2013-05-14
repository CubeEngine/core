package de.cubeisland.cubeengine.conomy.account.exp;

import de.cubeisland.cubeengine.conomy.account.AccountManager;
import de.cubeisland.cubeengine.conomy.account.BankAccount;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.account.storage.AccountStorage;
import de.cubeisland.cubeengine.conomy.Currency;

public class ExpBankAccount extends BankAccount
{
    public ExpBankAccount(AccountManager manager, Currency currency, AccountModel model)
    {
        super(manager, currency, model);
    }

    @Override
    public boolean deposit(double amount)
    {
        this.model.value += amount;
        this.update();
        return true;
    }

    @Override
    public boolean withdraw(double amount)
    {
        this.model.value -= amount;
        this.update();
        return true;
    }

    @Override
    public boolean set(double amount)
    {
        this.model.value = (int)amount;
        this.update();
        return true;
    }

    @Override
    public boolean has(double amount)
    {
        if ((this.model.value - amount * this.getCurrency().fractionalDigitsFactor())
            < this.getCurrency().getMinBankMoney())
        {
            return false;
        }
        return true;
    }
}
