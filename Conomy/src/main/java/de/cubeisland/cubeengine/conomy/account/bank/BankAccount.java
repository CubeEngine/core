package de.cubeisland.cubeengine.conomy.account.bank;

import de.cubeisland.cubeengine.conomy.account.AccountModel;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;

@SingleKeyEntity(tableName = "bankaccount", primaryKey = "user", autoIncrement = false)
public class BankAccount extends AccountModel
{
    @Attribute(type = AttrType.INT)
    protected Long key;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    protected final String name;

    public BankAccount(long key, String name, double start)
    {
        this.key = key;
        this.name = name;
        this.set(start);
    }

    public BankAccount(String name, double start)
    {
        this.name = name;
        this.set(start);
    }

    public BankAccount(String name)
    {
        this.name = name;
        this.reset();
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public Long getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Long id)
    {
        this.key = id;
    }
}
