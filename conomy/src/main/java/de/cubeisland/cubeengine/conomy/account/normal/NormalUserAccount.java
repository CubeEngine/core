package de.cubeisland.cubeengine.conomy.account.normal;

import de.cubeisland.cubeengine.conomy.account.UserAccount;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.account.storage.AccountStorage;
import de.cubeisland.cubeengine.conomy.currency.Currency;

public class NormalUserAccount extends UserAccount
{
    private AccountStorage storage;

    @Override
    public String getName()
    {
        return this.getHolder().getName();
    }

    @Override
    public void deposit(double amount)
    {
        this.model.value += amount * this.getCurrency().fractionalDigitsFactor();
        storage.update(model);
    }

    @Override
    public void withdraw(double amount)
    {
        this.model.value -= amount * this.getCurrency().fractionalDigitsFactor();
        storage.update(model);
    }

    @Override
    public boolean has(double amount)
    {
        if (false)// TODO permission to get under min
        {
            return true;
        }
        if ((this.model.value - amount * this.getCurrency().fractionalDigitsFactor()) < this.getCurrency().getMinMoney())
        {
            return false;
        }
        return true;
    }

    public void init(Currency currency, AccountModel model, AccountStorage storage)
    {
        super.init(currency, model);
        this.storage = storage;
    }

    @Override
    public void set(double amount)
    {
        this.model.value = (long)(amount * this.getCurrency().fractionalDigitsFactor());
        storage.update(model);
    }

    @Override
    public void scale(float factor)
    {
        this.model.value *= factor;
        storage.update(model);
    }
}
