package de.cubeisland.cubeengine.conomy.account.bank;

import de.cubeisland.cubeengine.conomy.account.AccountModel;

/**
 *
 * @author Faithcaio
 */
public class BankAccount extends AccountModel
{
    private final String name;
    private int key;
    
    public BankAccount(int key, String name, double start)
    {
        this.key = key;
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
    public Integer getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Integer id)
    {
        this.key = id;
    }
}
