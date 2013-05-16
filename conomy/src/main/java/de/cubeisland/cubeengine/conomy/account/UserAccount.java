package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserAttachment;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.Currency;
import de.cubeisland.cubeengine.conomy.Currency.CurrencyType;

public abstract class UserAccount extends UserAttachment implements Account
{
    private Currency currency;
    protected AccountModel model;
    private boolean isInit = false;
    protected AccountManager manager;

    public User getUser()
    {
        return this.getHolder();
    }

    @Override
    public boolean transactionTo(Account to, double amount, boolean force)
    {
        return this.manager.transaction(this,to,amount,force);
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

    protected void init(AccountManager manager, Currency currency, AccountModel model)
    {
        this.manager = manager;
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

    protected void update()
    {
        this.manager.storage.update(this.model);
    }

    @Override
    public String getName()
    {
        return this.getHolder().getName();
    }

    @Override
    public boolean reset()
    {
        this.set(this.currency.getDefaultBalance());
        return true; // TODO override if not possible!!! (for other currencyTypes than NORMAL)
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
}
