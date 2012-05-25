package de.cubeisland.cubeengine.conomy.account.user;

import de.cubeisland.cubeengine.conomy.account.IAccount;
import de.cubeisland.cubeengine.core.user.User;

/**
 *
 * @author Faithcaio
 */
public class UserAccount implements IAccount
{
    private double balance;
    private final User user;
    
    public UserAccount(User user, double start)
    {
        this.user = user;
        this.balance = start;
    }
    
    public UserAccount(User user)
    {
        this.user = user;
        this.balance = 0;
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
    
    public User getUser()
    {
        return this.user;
    }
}
