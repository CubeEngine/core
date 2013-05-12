package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.conomy.currency.Currency.CurrencyType;

public abstract class BankAccount implements Account
{
    private String name;
    private Currency currency;
    protected AccountModel model;

    protected BankAccount(String name, Currency currency, AccountModel model)
    {
        this.name = name;
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
        return this.name;
    }

    @Override
    public boolean transaction(Account from, Account to, double amount, boolean force)
    {
        if (from.getCurrencyType().equals(to.getCurrencyType()))
        {
            if (!force)
            {
                if (from.has(amount))
                {

                }
            }
            from.withdraw(amount);
            to.deposit(amount);
        }
        return false;
    }
}
