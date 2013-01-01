package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.currency.Currency;
import java.util.Collection;

/**
 * Interface for an account
 */
public interface IAccount
{
    /**
     * Adds money in given currency to this account. The amount is in the lowest
     * sub-currency of given currency
     *
     * @param amount the amount to give
     * @param currencyName the currency
     * @return the new amount
     */
    public ConomyResponse give(long amount, String currencyName);

    /**
     * Takes money in given currency away from this account. The amount is the
     * in lowest sub-currency of given currency
     *
     * @param amount the amount to take
     * @param currencyName the currency
     * @return the new balance
     */
    public ConomyResponse take(long amount, String currencyName);

    /**
     * Transfers money in given currency from an other account to this one. The
     * amount is in the lowest sub-currency of given currency
     *
     * @param source the account to take the money from
     * @param amount the amount to deposit
     * * @param currency the currency
     * @return the new balance
     */
    public ConomyResponse deposit(IAccount target, long amount, String currencyName);

    /**
     * Transfers money in given currency from this account to an other one. The
     * amount is in the lowest sub-currency of given currency
     *
     * @param target the account to give the money to
     * @param amount the amount to withdraw
     * @param currency the currency
     * @return the new balance
     */
    public ConomyResponse withdraw(IAccount source, long amount, String currencyName);

    /**
     * Returns the balance in given currency. The amount is in the lowest
     * sub-currency of given currency
     *
     * @param currency the currency
     * @return the balance
     */
    public ConomyResponse balance(String currencyName);

    /**
     * Resets the balance of given currency to 0
     *
     * @param currency the currency
     */
    public ConomyResponse reset(String currencyName);

    /**
     * Resets the balance in all currencies to 0.
     */
    public void resetAll();

    /**
     * Resets the balance in all currencies to its defined default value.
     */
    public void resetAllToDefault();

    /**
     * Resets the balance of given currency to its defined default value.
     */
    public void resetToDefault(String currencyName);

    /**
     * Sets the balance to the specified amount for given currency. The amount
     * is in the lowest sub-currency of given currency
     *
     *
     * @param amount the amount to set
     * @param currencyName the currency
     */
    public ConomyResponse set(long amount, String currencyName);

    /**
     * Scales the balance of given currency with the given factor (always
     * rounding down if necessary)
     *
     * @param factor the factor to scale with
     * @param currency the currency
     * @return the new balance
     */
    public ConomyResponse scale(double factor, String currencyName);

    /**
     * Returns true if the currency is supported by this account
     *
     * @param currency
     * @return
     */
    public boolean doesSupport(String currencyName);

    /**
     * Returns true if the account is bound to a user
     *
     * @return
     */
    public boolean isUserAccount();

    public Collection<CurrencyAccount> getCurrencyAccounts();

    public CurrencyAccount getCurrencyAccount(String currencyName);
}
