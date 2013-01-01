package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.storage.Model;

public class Account implements Model<Long>
{//TODO make this to a model
    private long key = -1;
    private long account_id; //Id of a User or BankAccount
    private long balance;
    private final Currency currency;
    private String currencyName;

    public Account(Currency currency)
    {
        this.currency = currency;
        this.currencyName = currency.getName();

    }

    public long getBalance()
    {
        return balance;
    }

    public void setBalance(long balance)
    {
        this.balance = balance;
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
}
