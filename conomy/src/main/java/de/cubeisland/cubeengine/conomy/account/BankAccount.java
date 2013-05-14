package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.Currency;
import de.cubeisland.cubeengine.conomy.Currency.CurrencyType;

public abstract class BankAccount implements Account
{
    private Currency currency;
    protected AccountModel model;
    private AccountManager manager;

    protected BankAccount(AccountManager manager, Currency currency, AccountModel model)
    {
        this.manager = manager;
        this.currency = currency;
        this.model = model;
    }

    @Override
    public Currency getCurrency()
    {
        return this.currency;
    }

    @Override
    public CurrencyType getCurrencyType()
    {
        return this.currency.getType();
    }

    @Override
    public String getName()
    {
        return this.model.name;
    }

    @Override
    public boolean transactionTo(Account to, double amount, boolean force)
    {
        return this.manager.transaction(this,to,amount,force);
    }

    protected void update()
    {
        this.manager.storage.update(this.model);
    }


    @Override
    public boolean isHidden()
    {
        return this.model.hidden;
    }

    @Override
    public void setHidden(boolean hidden)
    {
        this.model.hidden = hidden;
        this.update();
    }

    @Override
    public boolean scale(float factor)
    {
        return this.set(this.balance() * factor);
    }

    @Override
    public double balance()
    {
        return this.model.value / this.currency.fractionalDigitsFactor();
    }

    @Override
    public boolean reset()
    {
        return this.set(this.currency.getDefaultBankBalance());
    }
}
