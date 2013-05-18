package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserAttachment;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.ConomyPermissions;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;

public class UserAccount extends UserAttachment implements Account
{
    private boolean isInit = false;

    protected AccountModel model;
    protected ConomyManager manager;

    public User getUser()
    {
        return this.getHolder();
    }

    @Override
    public Conomy getModule()
    {
        return (Conomy)super.getModule();
    }

    protected void init(ConomyManager manager, AccountModel model)
    {
        this.manager = manager;
        this.model = model;
        this.isInit = true;
    }

    protected void update()
    {
        this.manager.storage.update(this.model);
    }

    public boolean isInitialized()
    {
        return this.isInit;
    }

    @Override
    public String getName()
    {
        return this.getHolder().getName();
    }

    @Override
    public boolean transactionTo(Account to, double amount, boolean force)
    {
        return this.manager.transaction(this,to,amount,force);
    }

    @Override
    public boolean reset()
    {
        this.set(this.manager.getDefaultBalance());
        return true;
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
    public double balance()
    {
        return this.model.value / this.manager.fractionalDigitsFactor();
    }

    @Override
    public boolean has(double amount)
    {
        if (ConomyPermissions.ACCOUNT_ALLOWUNDERMIN.isAuthorized(this.getHolder()))
        {
            return true;
        }
        if ((this.model.value - amount * this.manager.fractionalDigitsFactor()) < this.manager.getMinimumBalance())
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean scale(float factor)
    {
        boolean b = this.set(this.balance() * factor);
        this.manager.logger.info("SCALE User:" + this.getName() + " " + factor + " :: " + this.balance());
        return b;
    }

    @Override
    public boolean deposit(double amount)
    {
        this.model.value += amount * this.manager.fractionalDigitsFactor();
        this.update();
        this.manager.logger.info("DEPOSIT User:" + this.getName() + " " + amount + " :: " + this.balance());
        return true;
    }

    @Override
    public boolean withdraw(double amount)
    {
        this.model.value -= amount * this.manager.fractionalDigitsFactor();
        this.update();
        this.manager.logger.info("WITHDRAW User:" + this.getName() + " " + amount + " :: " + this.balance());
        return true;
    }

    @Override
    public boolean set(double amount)
    {
        this.model.value = (long)(amount * this.manager.fractionalDigitsFactor());
        this.update();
        this.manager.logger.info("SET User:" + this.getName() + " " + amount + " :: " + amount);
        return true;
    }
}
