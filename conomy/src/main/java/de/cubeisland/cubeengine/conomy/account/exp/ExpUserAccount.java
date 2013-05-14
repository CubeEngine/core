package de.cubeisland.cubeengine.conomy.account.exp;

import de.cubeisland.cubeengine.conomy.account.UserAccount;

public class ExpUserAccount extends UserAccount
{
    // TODO is amount levels OR exp ???
    // done with exp for now
    @Override
    public boolean deposit(double amount)
    {
        this.getHolder().setExp(this.getHolder().getExp() + (float)amount);
        return true;
    }

    @Override
    public boolean withdraw(double amount)
    {
        this.getHolder().setExp(this.getHolder().getExp() - (float)amount);
        return true;
    }

    @Override
    public boolean set(double amount)
    {
        this.getHolder().setExp((float)amount);
        return true;
    }

    @Override
    public boolean has(double amount)
    {
        return this.getHolder().getExp() >= amount;
    }
}
