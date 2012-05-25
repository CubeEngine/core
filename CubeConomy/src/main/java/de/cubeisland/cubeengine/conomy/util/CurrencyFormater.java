package de.cubeisland.cubeengine.conomy.util;

import de.cubeisland.cubeengine.conomy.account.AccountModel;

/**
 *
 * @author Faithcaio
 */
public class CurrencyFormater
{
    public String convertBalance(double amount)
    {
        String out;
        int major = (int)amount;
        int minor = ((int)(amount*100))%100;
        //TODO
        out = major + "C " + minor + "p"; 
        return out;
    }
    
    public String convertBalance(AccountModel model)
    {
        return this.convertBalance(model.balance());
    }
    
}
