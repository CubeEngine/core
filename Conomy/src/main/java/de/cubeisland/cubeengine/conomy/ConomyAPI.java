package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.conomy.account.Account;
import de.cubeisland.cubeengine.conomy.account.ConomyResponse;
import de.cubeisland.cubeengine.conomy.account.CurrencyAccount;
import de.cubeisland.cubeengine.conomy.account.IAccount;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.user.User;
import java.util.Collection;

public class ConomyAPI
{
    private final Conomy module;

    public ConomyAPI(Conomy conomy)
    {
        this.module = conomy;
    }

    /**
     * Returns the main currency (first currency declared in the configuration)
     *
     * @return
     */
    public Currency getMainCurrency()
    {
        return this.module.getCurrencyManager().getMainCurrency();
    }

    /**
     * Gets all currently used currencies
     *
     * @return
     */
    public Collection<Currency> getAllCurrencies()
    {
        return this.module.getCurrencyManager().getAllCurrencies();
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
    public IAccount createAccount(String name, Currency currency)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Creates an account bound to a specific user
     *
     * @param user
     * @return
     */
    public IAccount createUserAccount(User user)
    {
        return this.module.getAccountsManager().createNewAccount(user);
    }

    /**
     * Returns the UserAccount for given user or null if the user does not have
     * an account
     *
     * @param user
     * @return
     */
    public IAccount getUserAccount(User user)
    {
        return this.module.getAccountsManager().getAccount(user);
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
        return this.transaction(source, target, this.getMainCurrency(), amount);
    }

    public boolean transaction(IAccount source, IAccount target, Currency currency, long amount)
    {
        if (source.doesSupport(currency.getName()) && target.doesSupport(currency.getName()))
        {
            source.take(amount, currency.getName());
            target.give(amount, currency.getName());
            this.module.getCurrencyAccountManager().update(source.getCurrencyAccount(currency.getName()));
            this.module.getCurrencyAccountManager().update(target.getCurrencyAccount(currency.getName()));
        }
        return false;
    }

    /**
     * Returns if the user has an account on the server yet.
     *
     * @param user
     * @return if the player has an account
     */
    public boolean hasAccount(User user)
    {
        return this.module.getAccountsManager().hasAccount(user);
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
        if (this.hasAccount(user))
        {
            IAccount acc = this.getUserAccount(user);
            return acc.doesSupport(currency.getName());
        }
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
        return this.getBalance(user, this.getMainCurrency());
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
        if (this.hasAccount(user))
        {
            ConomyResponse response = this.module.getAccountsManager().getAccount(user).balance(currency.getName());
            if (response.success)
            {
                return response.balance;
            }
        }
        return null;
    }

    /**
     * Resets all currency-accounts of this user to the default vaöue
     *
     * @param user
     */
    public boolean resetAllAccountsToDefault(User user)
    {
        if (this.hasAccount(user))
        {
            Account acc = this.module.getAccountsManager().getAccount(user);
            acc.resetAllToDefault();
            for (CurrencyAccount cAcc : acc.getCurrencyAccounts())
            {
                this.module.getCurrencyAccountManager().update(cAcc);
            }
            return false;
        }
        return false;
    }
}
