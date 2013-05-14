package de.cubeisland.cubeengine.conomy.account.normal;

import de.cubeisland.cubeengine.conomy.account.UserAccount;

public class NormalUserAccount extends UserAccount
{
    @Override
    public String getName()
    {
        return this.getHolder().getName();
    }

    @Override
    public boolean deposit(double amount)
    {
        this.model.value += amount * this.getCurrency().fractionalDigitsFactor();
        this.update();
        return true;
    }

    @Override
    public boolean withdraw(double amount)
    {
        this.model.value -= amount * this.getCurrency().fractionalDigitsFactor();
        this.update();
        return true;
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

    @Override
    public boolean set(double amount)
    {
        this.model.value = (long)(amount * this.getCurrency().fractionalDigitsFactor());
        this.update();
        return true;
    }
}
