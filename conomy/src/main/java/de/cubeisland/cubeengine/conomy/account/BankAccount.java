package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;

public class BankAccount implements Account
{
    protected AccountModel model;
    private ConomyManager manager;

    protected BankAccount(ConomyManager manager, AccountModel model)
    {
        this.manager = manager;
        this.model = model;
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
    public double balance()
    {
        return this.model.value / this.manager.fractionalDigitsFactor();
    }

    @Override
    public boolean scale(float factor)
    {
        boolean b = this.set(this.balance() * factor);
        this.manager.logger.info("SCALE Bank:" + this.getName() + " " + factor + " :: " + this.balance());
        return b;
    }

    @Override
    public boolean reset()
    {
        return this.set(this.manager.getDefaultBankBalance());
    }

    @Override
    public boolean deposit(double amount)
    {
        this.model.value += amount * this.manager.fractionalDigitsFactor();
        this.update();
        this.manager.logger.info("DEPOSIT Bank:" + this.getName() + " " + amount + " :: " + this.balance());
        return true;
    }

    @Override
    public boolean withdraw(double amount)
    {
        this.model.value -= amount * this.manager.fractionalDigitsFactor();
        this.update();
        this.manager.logger.info("WITHDRAW Bank:" + this.getName() + " " + amount + " :: " + this.balance());
        return true;
    }

    @Override
    public boolean has(double amount)
    {
        return (this.model.value - amount * this.manager.fractionalDigitsFactor()) >= this.manager.getMinimumBankBalance();
    }

    @Override
    public boolean set(double amount)
    {
        this.model.value = (long)(amount * this.manager.fractionalDigitsFactor());
        this.update();
        this.manager.logger.info("SET Bank:" + this.getName() + " " + amount + " :: " + amount);
        return true;
    }
}
