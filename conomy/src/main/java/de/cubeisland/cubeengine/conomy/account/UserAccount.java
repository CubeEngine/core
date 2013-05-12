package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserAttachment;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.conomy.currency.Currency.CurrencyType;

public abstract class UserAccount extends UserAttachment implements Account
{
    private Currency currency;
    protected AccountModel model;
    private boolean isInit = false;

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

    public User getUser()
    {
        return this.getHolder();
    }

    @Override
    public CurrencyType getCurrencyType()
    {
        return this.getCurrency().getType();
    }

    @Override
    public Conomy getModule()
    {
        return this.getModule();
    }

    protected void init(Currency currency, AccountModel model)
    {
        this.currency = currency;
        this.model = model;
        this.isInit = true;
    }

    @Override
    public Currency getCurrency()
    {
        return this.currency;
    }

    public boolean isInitialized()
    {
        return this.isInit;
    }
}
