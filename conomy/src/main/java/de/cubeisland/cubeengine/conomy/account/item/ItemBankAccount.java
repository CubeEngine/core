package de.cubeisland.cubeengine.conomy.account.item;

import de.cubeisland.cubeengine.conomy.account.AccountManager;
import de.cubeisland.cubeengine.conomy.account.BankAccount;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.account.storage.AccountStorage;
import de.cubeisland.cubeengine.conomy.Currency;

public class ItemBankAccount extends BankAccount
{
    public ItemBankAccount(AccountManager manager, Currency currency, AccountModel model)
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

    @Override
    public void deposit(double amount)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void withdraw(double amount)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void set(double amount)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void scale(float factor)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean has(double amount)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
