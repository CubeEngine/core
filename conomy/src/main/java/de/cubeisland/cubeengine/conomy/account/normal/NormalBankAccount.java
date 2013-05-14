package de.cubeisland.cubeengine.conomy.account.normal;

import de.cubeisland.cubeengine.conomy.account.AccountManager;
import de.cubeisland.cubeengine.conomy.account.BankAccount;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.account.storage.AccountStorage;
import de.cubeisland.cubeengine.conomy.Currency;

public class NormalBankAccount extends BankAccount
{
    public NormalBankAccount(AccountManager manager, Currency currency, AccountModel model)
    {
        super(manager,currency,model);
    }

    @Override
    public boolean deposit(double amount)
    {
        this.model.value += amount * this.getCurrency().fractionalDigitsFactor();
        this.update();
        return true;
    }

    @Override
    public boolean withdraw(double amount)
    {
        this.model.value -= amount * this.getCurrency().fractionalDigitsFactor();
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

    @Override
    public boolean set(double amount)
    {
        this.model.value = (long)(amount * this.getCurrency().fractionalDigitsFactor());
        this.update();
        return true;
    }
}
