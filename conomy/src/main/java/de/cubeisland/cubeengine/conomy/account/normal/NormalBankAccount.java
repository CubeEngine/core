package de.cubeisland.cubeengine.conomy.account.normal;

import de.cubeisland.cubeengine.conomy.account.BankAccount;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.account.storage.AccountStorage;
import de.cubeisland.cubeengine.conomy.currency.Currency;

public class NormalBankAccount extends BankAccount
{
    private AccountStorage storage;

    public NormalBankAccount(String name, Currency currency, AccountModel model, AccountStorage storage)
    {
        super(name,currency,model);
        this.storage = storage;
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
        if ((this.model.value - amount * this.getCurrency().fractionalDigitsFactor())
            < this.getCurrency().getMinBankMoney())
        {
            return false;
        }
        return true;
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
