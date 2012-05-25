package de.cubeisland.cubeengine.conomy.account;

/**
 *
 * @author Faithcaio
 */
public interface IAccount
{
    public double give(double amount); //Create Money
    public double take(double amount); //Delete Money
    public double deposit(IAccount acc, double amount); //Transfer Money acc->Account
    public double withdraw(IAccount acc, double amount);//Transfer Money Account->acc
    public double balance();
    public void reset();
    public void set(double amount);
    public double scale(double factor);            
}
