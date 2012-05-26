package de.cubeisland.cubeengine.conomy.util;

import de.cubeisland.cubeengine.conomy.account.AccountModel;

/**
 *
 * @author Faithcaio
 */
public class CurrencyFormater
{
    public String convertBalance(double amount, String format)
    {
        int major = (int)amount;
        int minor = ((int)(amount*100))%100;
        StringBuilder sb = new StringBuilder();
        int max = format.length();
        for (int i=0 ; i<max ; ++i)
        {
            if (format.charAt(i) == 'M')
            {
                if (i < max-1 && format.charAt(i+1) == 'M')
                {
                    sb.append("Cubes");//TODO config.MM etc...
                    ++i;
                    continue;
                }
                sb.append("C");
                continue;
            }
            if (format.charAt(i) == 'm')
            {
                if (i < max-1 && format.charAt(i+1) == 'm')
                {
                    sb.append("Points");
                    ++i;
                    continue;
                }
                sb.append("p");
                continue;
            }
            if (format.charAt(i) == 'D')
            {
                sb.append(major);
                continue;
            }
            if (format.charAt(i) == 'd')
            {
                sb.append(minor);
                continue;
            }
            sb.append(format.charAt(i));
        }
        return sb.toString();
        
/*
individual settings possible e.g for signs
 
Example Format in Config for amount = 1.30:
M = "C"
m = "p"
MM = Cubes
mm = Points
"DM dm" -> 1C 30p 
"D.dM" -> 1.30C 
"D,dM" -> 1,30C
"D,dMM" -> 1,30Cubes
"Dd mm" -> 130 Points
"MD.d" -> C1.30
*/
    }
    
    public String convertBalance(double balance)
    {
        return this.convertBalance(balance, null);//TODO config.format
    }
    
    public String convertBalance(AccountModel model)
    {
        return this.convertBalance(model.balance());
    }
    
}
