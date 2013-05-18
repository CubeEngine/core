package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;

public class BankAccount extends Account
{
    protected BankAccount(ConomyManager manager, AccountModel model)
    {
        super(manager, model);
    }

    @Override
    public String getName()
    {
        return this.model.name;
    }

    @Override
    protected void log(String action, Object value)
    {
        this.manager.logger.info(action + " Bank:" + this.getName() + " " + value + " :: " + this.balance());
    }

    /**
     * Deletes this BankAccount
     */
    public void delete()
    {
        this.manager.deleteBankAccount(this.getName());
    }

    @Override
    public boolean has(double amount)
    {
        return (this.model.value - amount * this.manager.fractionalDigitsFactor()) >= this.manager.getMinimumBankBalance();
    }

    @Override
    public double getDefaultBalance()
    {
        return this.manager.getDefaultBankBalance();
    }

    @Override
    public double getMinBalance()
    {
        return this.manager.getMinimumBankBalance();
    }
}
