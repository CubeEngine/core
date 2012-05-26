package de.cubeisland.cubeengine.conomy.account.bank;

import de.cubeisland.cubeengine.conomy.account.AccountModel;

/**
 *
 * @author Faithcaio
 */
public class BankAccount extends AccountModel
{
    private final String name;
    private int id;
    
    public BankAccount(int id, String name, double start)
    {
        this.id = id;
        this.name = name;
        this.set(start);
    }
    
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

    @Override
    public int getId()
    {
        return this.id;
    }

    @Override
    public void setId(int id)
    {
        this.id = id;
    }
}
