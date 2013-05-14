package de.cubeisland.cubeengine.conomy.account.item;

import de.cubeisland.cubeengine.conomy.account.UserAccount;

public class ItemUserAccount extends UserAccount
{
    @Override
    public boolean deposit(double amount)
    {
        if (false) // TODO check for place
        {
            return false;
        }
        // TODO put currency items into inventory
        return true;
    }

    @Override
    public boolean withdraw(double amount)
    {
        if (this.has(amount))
        {
            // TODO remove currency items from inventory
            return true;
        }
        return false;
    }

    @Override
    public boolean set(double amount)
    {
        if (false)
        {
            return false;// TODO check for place
        }
        // TODO set currency items in inventory
        return true;
    }

    @Override
    public boolean has(double amount)
    {
        // TODO check amount
        return false;
    }

    public int countCurrency()
    {
        return 0; // TODO count currency items in inventory (+bank?)
    }
}
