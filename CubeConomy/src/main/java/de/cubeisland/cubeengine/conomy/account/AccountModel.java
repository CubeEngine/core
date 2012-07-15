package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.core.persistence.Model;

/**
 *
 * @author Anselm Brehme
 */
public abstract class AccountModel implements IAccount,Model<Integer>
{
    private double balance;
    
    /**
     * Returns the Name of this Account
     * Bank Accounts : #BankName
     * Player Account : PlayerName
     * 
     * @return the AccountName
     */
    public abstract String getName();
    public abstract Integer getKey();
    public abstract void setKey(Integer id);

    public double give(double amount)
    {
        return (this.balance += amount);
    }

    public double take(double amount)
    {
        return (this.balance -= amount);
    }

    public double deposit(IAccount acc, double amount)
    {
        acc.take(amount);
        return this.give(amount);
    }

    public double withdraw(IAccount acc, double amount)
    {
        acc.give(amount);
        return this.take(amount);
    }

    public double balance()
    {
        return this.balance;
    }

    public void reset()
    {
        this.balance = 0;
    }

    public void set(double amount)
    {
        this.balance = amount;
    }

    public double scale(double factor)
    {
        return (this.balance *= factor);
    }
}
