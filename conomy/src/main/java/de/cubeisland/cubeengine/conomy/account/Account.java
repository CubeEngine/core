package de.cubeisland.cubeengine.conomy.account;

public interface Account
{
    /**
     * Returns the name of this account
     *
     * @return
     */
    public String getName();

    /**
     * Performs a transaction from this account to another.
     *
     * @param to the account to transfer to
     * @param amount the amount to transfer
     * @param force if true do not check if balance would go under minimum
     * @return true if the transaction was successful
     */
    boolean transactionTo(Account to, double amount, boolean force);

    /**
     * Adds the specified amount into this account
     *
     * @param amount the amount to add
     * @return true if the amount was added successfully
     */
    boolean deposit(double amount);

    /**
     * Removes the specified amount from this account
     *
     * @param amount the amount to remove
     * @return true if the amount was removed successfully
     */
    boolean withdraw(double amount);

    /**
     * Sets this account to the specified balance
     *
     * @param amount the new balance to set
     * @return false when not supported
     */
    boolean set(double amount);

    /**
     * Scales the balance of this account with the specified factor.
     *
     * @param factor the factor to scale with
     * @return false when not supported
     */
    boolean scale(float factor);

    /**
     * Returns whether this account can afford the specified amount
     *
     * @param amount the amount to check for
     * @return true if the account has sufficient balance
     */
    boolean has(double amount);

    /**
     * Resets the balance of this account to the default-balance specified in the configuration
     *
     * @return false when not supported
     */
    boolean reset(); // returns false when not supported

    /**
     * Returns the hidden state of this account
     *
     * @return true if the account is hidden
     */
    boolean isHidden();

    /**
     * Sets the hidden state of this account
     *
     * @param hidden true to hide this account
     */
    void setHidden(boolean hidden);

    /**
     * Returns the current balance
     *
     * @return
     */
    double balance();
}
