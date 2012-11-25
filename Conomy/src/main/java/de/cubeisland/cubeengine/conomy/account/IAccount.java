package de.cubeisland.cubeengine.conomy.account;

public interface IAccount
{
    /**
     * Gives this account money
     *
     * @param amount the amount to give
     * @return the new balance
     */
    public double give(double amount);

    /**
     * Takes money away from this account
     *
     * @param amount the amount to ake
     * @return the new balance
     */
    public double take(double amount);

    /**
     * Transfers money from an other account to this
     *
     * @param acc    the account to take the money from
     * @param amount the amount to deposit
     * @return the new balance
     */
    public double deposit(IAccount acc, double amount);

    /**
     * Transfers money from this account to an other
     *
     * @param acc    the account to give the money to
     * @param amount the amount to withdraw
     * @return the new balance
     */
    public double withdraw(IAccount acc, double amount);

    /**
     * Returns the balance
     *
     * @return the balance
     */
    public double balance();

    /**
     * Resets the balance to 0
     */
    public void reset();

    /**
     * Sets the balance to the specified amount
     *
     * @param amount the amount to set
     */
    public void set(double amount);

    /**
     * Scales the balance with the given factor
     *
     * @param factor the factor to scale with
     * @return the new balance
     */
    public double scale(double factor);
}
