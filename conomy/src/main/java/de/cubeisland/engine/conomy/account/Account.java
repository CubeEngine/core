/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.conomy.account;

import de.cubeisland.engine.conomy.account.storage.AccountModel;

public abstract class Account
{
    protected final ConomyManager manager;
    final AccountModel model; // package private!

    public Account(ConomyManager manager, AccountModel model)
    {
        this.manager = manager;
        this.model = model;
    }

    /**
     * Returns the name of this account
     *
     * @return
     */
    public abstract String getName();

    /**
     * Logs an action for this account formatted like this:
     * <p>{@code action} accountName {@code value} :: current balance
     */
    public abstract void log(String action, Object value);

    /**
     * Updates the account in the database
     */
    protected void update()
    {
        this.model.update();
    }

    /**
     * Performs a transaction from this account to another.
     *
     * @param to the account to transfer to
     * @param amount the amount to transfer
     * @param force if true do not check if balance would go under minimum
     * @return true if the transaction was successful
     */
    public boolean transactionTo(Account to, double amount, boolean force)
    {
        return this.manager.transaction(this,to,amount,force);
    }

    private void set0(long amount)
    {
        this.model.setValue(amount);
        this.update();
    }

    /**
     * Adds the specified amount into this account
     *
     * @param amount the amount to add
     */
    public void deposit(double amount)
    {
        this.set0(this.model.getValue() + (long)(amount * this.manager.fractionalDigitsFactor()));
        this.log("DEPOSIT", amount);
    }

    /**
     * Removes the specified amount from this account
     *
     * @param amount the amount to remove
     */
    public void withdraw(double amount)
    {
        this.set0(this.model.getValue() - (long)(amount * this.manager.fractionalDigitsFactor()));
        this.log("WITHDRAW" , amount);
    }

    /**
     * Sets this account to the specified balance
     *
     * @param amount the new balance to set
     */
    public void set(double amount)
    {
        this.set0((long)(amount * this.manager.fractionalDigitsFactor()));
        this.log("SET" , "");
    }

    /**
     * Scales the balance of this account with the specified factor.
     *
     * @param factor the factor to scale with
     */
    public void scale(float factor)
    {
        this.set0((long)(this.model.getValue() * factor));
        this.log("SCALE" , factor);
    }

    /**
     * Resets the balance of this account to the default-balance specified in the configuration
     */
    public void reset()
    {
        this.set0((long)(this.getDefaultBalance() * this.manager.fractionalDigitsFactor()));
        this.log("RESET" , "");
    }

    /**
     * Returns the hidden state of this account
     *
     * @return true if the account is hidden
     */
    public boolean isHidden()
    {
        return this.model.isHidden();
    }

    /**
     * Sets the hidden state of this account
     *
     * @param hidden true to hide this account
     */
    public void setHidden(boolean hidden)
    {
        this.model.setHidden(hidden);
        this.update();
        this.log(hidden ? "HIDE" : "UNHIDE", "");
    }
    /**
     * Returns the current balance
     *
     * @return the current balance
     */
    public double balance()
    {
        return (double)this.model.getValue() / this.manager.fractionalDigitsFactor();
    }

    /**
     * Returns whether this account can afford the specified amount
     *
     * @param amount the amount to check for
     * @return true if the account has sufficient balance
     */
    public abstract boolean has(double amount);

    public abstract double getDefaultBalance();
    public abstract double getMinBalance();
}
