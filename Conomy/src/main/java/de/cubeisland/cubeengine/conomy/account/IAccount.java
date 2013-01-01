package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.currency.Currency;

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
     * @param currency the currency
     * @return the new amount
     */
    public ConomyResponse give(long amount, Currency currency);

    /**
     * Takes money in given currency away from this account. The amount is the
     * in lowest sub-currency of given currency
     *
     * @param amount the amount to take
     * @param currency the currency
     * @return the new balance
     */
    public ConomyResponse take(long amount, Currency currency);

    /**
     * Transfers money in given currency from an other account to this one. The
     * amount is in the lowest sub-currency of given currency
     *
     * @param source the account to take the money from
     * @param amount the amount to deposit
     * * @param currency the currency
     * @return the new balance
     */
    public ConomyResponse deposit(IAccount target, long amount, Currency currency);

    /**
     * Transfers money in given currency from this account to an other one. The
     * amount is in the lowest sub-currency of given currency
     *
     * @param target the account to give the money to
     * @param amount the amount to withdraw
     * @param currency the currency
     * @return the new balance
     */
    public ConomyResponse withdraw(IAccount source, long amount, Currency currency);

    /**
     * Returns the balance in given currency. The amount is in the lowest
     * sub-currency of given currency
     *
     * @param currency the currency
     * @return the balance
     */
    public ConomyResponse balance(Currency currency);

    /**
     * Resets the balance of given currency to 0
     *
     * @param currency the currency
     */
    public ConomyResponse reset(Currency currency);

    /**
     * Resets the balance in all currencies to 0.
     */
    public void resetAll();

    /**
     * Sets the balance to the specified amount for given currency. The amount
     * is in the lowest sub-currency of given currency
     *
     *
     * @param amount the amount to set
     * @param currency the currency
     */
    public ConomyResponse set(long amount, Currency currency);

    /**
     * Scales the balance of given currency with the given factor (always
     * rounding down if necessary)
     *
     * @param factor the factor to scale with
     * @param currency the currency
     * @return the new balance
     */
    public ConomyResponse scale(double factor, Currency currency);

    /**
     * Returns true if the currency is supported by this account
     *
     * @param currency
     * @return
     */
    public boolean doesSupport(Currency currency);
    
    /**
     * Returns true if the account is bound to a user
     * 
     * @return 
     */
    public boolean isUserAccount();
}
