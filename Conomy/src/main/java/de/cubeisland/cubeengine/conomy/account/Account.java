package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;

public class Account
{//TODO hide account (dont show unless forced)
    private User user;
    private AccountModel model;
    private final Currency currency;
    private final AccountManager manager;

    public Account(AccountManager manager, Currency currency, AccountModel model)
    {
        this.manager = manager;
        this.currency = currency;
        this.model = model;
        this.user = model.user_id == null ? null : CubeEngine.getUserManager().getUser(model.user_id);
    }

    public User getUser()
    {
        return user;
    }

    public boolean isUserAccount()
    {
        return user != null;
    }

    public long getBalance()
    {
        return this.model.value;
    }

    public Currency getCurrency()
    {
        return currency;
    }

    private void updateModel()
    {
        this.manager.getStorage().update(this.model);
    }

    /**
     * Transfers given amount of money from the source-account to this one.
     *
     * @param source the source-account (can be null)
     * @param amount the amount to transfer (can be negative) in this accounts
     * currency
     * @throws IllegalArgumentException when currencies are not convertible
     * @return the new balance
     */
    public long transaction(Account source, long amount) throws IllegalArgumentException
    {
        if (source != null)
        {
            if (!source.currency.equals(this.currency))
            {
                if (!this.currency.canConvert(source.currency))
                {
                    throw new IllegalArgumentException("Cannot convert " + source.currency.getName() + " into " + this.currency.getName());
                }
                //TODO convert
            }
            else
            {
                source.model.value -= amount;
            }
            source.updateModel();
        }
        this.model.value += amount;
        this.updateModel();
        return this.model.value;
    }

    /**
     * Resets the balance to the defined default value.
     */
    public void resetToDefault()
    {
        this.model.value = this.currency.getDefaultValue();
        this.updateModel();
    }

    /**
     * Sets the balance to the specified amount.
     *
     * @param amount the amount to set
     */
    public void set(long amount)
    {
        this.model.value = amount;
        this.updateModel();
    }

    /**
     * Scales the balance with the given factor (always rounding down if
     * necessary)
     *
     * @param factor the factor to scale with
     * @return the new balance
     */
    public long scale(double factor)
    {
        this.model.value = (long) (factor * this.model.value);
        this.updateModel();
        return this.model.value;
    }
}