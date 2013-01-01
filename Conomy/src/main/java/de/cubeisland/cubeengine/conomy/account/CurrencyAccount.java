package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;

@SingleKeyEntity(tableName = "currencyaccount", primaryKey = "key", autoIncrement = true)
public class CurrencyAccount implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long key = -1;
    @Attribute(type = AttrType.INT, unsigned = true)
    @Index(value = Index.IndexType.FOREIGN_KEY, f_table = "accounts", f_field = "key")
    public long account_id; //Id of a User or BankAccount
    @Attribute(type = AttrType.INT, unsigned = true)
    public long balance;
    @Attribute(type = AttrType.VARCHAR, length = 64) //TODO limit in config
    public String currencyName;

    public CurrencyAccount()
    {
    }

    public CurrencyAccount(Currency currency, Account account)
    {
        this.currencyName = currency.getName();
        this.account_id = account.key;
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
