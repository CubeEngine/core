package de.cubeisland.cubeengine.conomy.account;

import org.bukkit.World;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.currency.Currency.CurrencyType;

public abstract class Account<Owner>
{
    private final Conomy module;

    private Owner owner;
    private World world;
    private AccountModel model;

    private final CurrencyType currencyType;



    public Account(Conomy module, CurrencyType currencyType)
    {
        this.module = module;
        this.currencyType = currencyType;
    }

    public Owner getOwner()
    {
        return this.owner;
    }

    public CurrencyType getCurrencyType()
    {
        return this.currencyType;
    }

    public abstract String getName();

    // TODO perhaps Object that contains detailed informations like Vaults ConomyResponse???
    public static boolean transaction(Account from, Account to, double amount, boolean force)
    {
        if (from.getCurrencyType().equals(to.getCurrencyType()))
        {
            if (!force)
            {
                if (from.has(amount))
                {

                }
            }
            from.withdraw(amount);
            to.deposit(amount);
        }
        return false;
    }

    public abstract void deposit(double amount);

    public abstract void withdraw(double amount);

    public abstract boolean has(double amount);

}
