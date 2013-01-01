package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.conomy.account.BankAccount;
import de.cubeisland.cubeengine.conomy.account.IAccount;
import de.cubeisland.cubeengine.conomy.account.UserAccount;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.user.User;
import java.util.Collection;

public class ConomyAPI
{
    /**
     * Returns the main currency (first currency declared in the configuration)
     *
     * @return
     */
    public Currency getMainCurrency()
    {
        return null;
    }

    /**
     * Gets all currently used currencies
     *
     * @return
     */
    public Collection<Currency> getAllCurrencies()
    {
        return null;
    }

    /**
     * Returns the amount of currencies currently used
     *
     * @return
     */
    public int currencyCount()
    {
        return this.getAllCurrencies().size();
    }

    /**
     * Creates an account with given name
     *
     * @param name
     * @param currency
     * @return
     */
    public BankAccount createAccount(String name, Currency currency)
    {
        return null;
    }

    /**
     * Creates an account bound to a specific user
     *
     * @param user
     * @return
     */
    public UserAccount createUserAccount(User user)
    {
        return null;
    }

    /**
     * Returns the UserAccount for given user or null if the user does not have
     * an account
     *
     * @param user
     * @return
     */
    public UserAccount getUserAccount(User user)
    {
        return null;
    }

    /**
     * Moves money from the source-account to the target-account
     *
     * @param source
     * @param target
     * @param amount the amount of money (using lowest sub-currency of source)
     * @return true if the transaction was succesful
     */
    public boolean transaction(IAccount source, IAccount target, long amount)
    {
        return false;
    }

    /**
     * Returns if the user has an account on the server yet (for the main
     * currency) this is equivalent to hasAccount(user, getMainCurrency())
     *
     * @param user
     * @return if the player has an account
     */
    public boolean hasAccount(User user)
    {
        return false;
    }

    /**
     * Returns if the user has an account for given currency on the server
     *
     * @param user
     * @param currency
     * @return
     */
    public boolean hasAccount(User user, Currency currency)
    {
        return false;
    }

    /**
     * Gets the balance of a user in the currencies lowest sub-currency (for the
     * main currency) this is equivalent to getBalance(user, getMainCurrency())
     *
     * @param user
     * @return
     */
    public Long getBalance(User user)
    {
        return null;
    }

    /**
     * Gets the balance of a user in the currencies lowest sub-currency
     *
     * @param user
     * @param currency
     * @return
     */
    public Long getBalance(User user, Currency currency)
    {
        return null;
    }
}
