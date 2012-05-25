package de.cubeisland.cubeengine.conomy.account.bank;

import de.cubeisland.cubeengine.conomy.account.IAccount;

/**
 *
 * @author Anselm
 */
public class BankAccount
{
    private double balance;
    private final String name;
    
    public BankAccount(String name, double start)
    {
        this.name = name;
        this.balance = start;
    }
    
    public BankAccount(String name)
    {
        this.name = name;
        this.balance = 0;
    }
    
    public String getName()
    {
        return this.name;
    }
    
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
