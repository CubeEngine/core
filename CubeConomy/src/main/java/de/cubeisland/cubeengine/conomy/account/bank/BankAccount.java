package de.cubeisland.cubeengine.conomy.account.bank;

import de.cubeisland.cubeengine.conomy.account.AccountModel;

/**
 *
 * @author Faithcaio
 */
public class BankAccount extends AccountModel
{
    private final String name;
    
    public BankAccount(String name, double start)
    {
        this.name = name;
        this.set(start);
    }
    
    public BankAccount(String name)
    {
        this.name = name;
        this.reset();
    }
    
    public String getName()
    {
        return this.name;
    }
}
