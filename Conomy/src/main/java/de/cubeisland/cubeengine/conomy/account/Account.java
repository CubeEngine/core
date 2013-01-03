package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.conomy.currency.CurrencyManager;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.user.User;

@SingleKeyEntity(tableName = "accounts", primaryKey = "key", autoIncrement = true)
public class Account implements Model<Long>
{//TODO hide account (dont show unless forced)
    @Attribute(type = AttrType.INT, unsigned = true)
    public long key = -1;
    @Attribute(type = AttrType.INT, unsigned = true, notnull = false)
    @Index(value = Index.IndexType.FOREIGN_KEY, f_table = "user", f_field = "key")
    public Long user_id;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String name;
    @Attribute(type = AttrType.VARCHAR, length = 64)
    public String currencyName;
    @Attribute(type = AttrType.INT, unsigned = true)
    public long value = 0;
    public Currency currency;

    public Account()
    {}

    public Account(Currency currency, User user)
    {
        this.user_id = user.key;
        this.currency = currency;
        this.currencyName = currency.getName();
        this.name = null;
        this.value = currency.getDefaultValue();
    }

    public Account(Currency currency, String name)
    {
        this.currency = currency;
        this.currencyName = currency.getName();
        this.user_id = null;
        this.name = name;
    }

    /**
     * Adds given amount of money to this account.
     *
     * @param amount the amount to give (can be negative)
     * @return the new amount
     */
    public long trancaction(long amount)
    {
        this.value += amount;
        return this.value;
    }

    /**
     * Transfers given amount of money from the source-Account to this one
     *
     * @param source the source-Account
     * @param amount the amount to transfer (can be negative)
     * @return
     */
    public long transaction(Account source, long amount) throws IllegalArgumentException
    {
        if (this.currency.canConvert(source.currency))
        {
            source.trancaction(amount);
            this.trancaction(amount);
            return this.value;
        }
        throw new IllegalArgumentException("Cannot convert " + source.currencyName + " into " + this.currencyName);
    }

    /**
     * Returns the current balance.
     *
     * @return the balance
     */
    public long balance()
    {
        return this.value;
    }

    /**
     * Resets the balance to the defined default value.
     */
    public void resetToDefault()
    {
        this.value = this.currency.getDefaultValue();
    }

    /**
     * Sets the balance to the specified amount.
     *
     * @param amount the amount to set
     */
    public void set(long amount)
    {
        this.value = amount;
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
        this.value = (long)(factor * this.value);
        return this.value;
    }

    /**
     * Returns true if the account is bound to a user
     *
     * @return
     */
    public boolean isUserAccount()
    {
        return this.user_id != null;
    }

    @Override
    public Long getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Long key)
    {
        this.key = key;
    }

    void setCurrency(CurrencyManager currencyManager)
    {
        this.currency = currencyManager.getCurrencyByName(currencyName);
    }
}
