package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.user.User;
import gnu.trove.map.hash.THashMap;
import java.util.Collection;

@SingleKeyEntity(tableName = "accounts", primaryKey = "key", autoIncrement = true)
public class Account implements IAccount, Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long key = -1;
    @Attribute(type = AttrType.INT, unsigned = true, notnull = false)
    @Index(value = Index.IndexType.FOREIGN_KEY, f_table = "user", f_field = "key")
    public Long user_id;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String name;
    //TODO currency limits in config too (e.g. min money set to 0 )
    private THashMap<String, CurrencyAccount> accounts = new THashMap<String, CurrencyAccount>();

    public Account()
    {
    }

    public Account(User user)
    {
        this.user_id = user.key;
        this.name = null;
    }

    public Account(String name)
    {
        this.user_id = null;
        this.name = name;
    }

    public void loadInCurrencyAccounts(Collection<CurrencyAccount> caccounts)
    {
        for (CurrencyAccount cacc : caccounts)
        {
            this.accounts.put(cacc.currencyName, cacc);
        }
    }

    @Override
    public ConomyResponse give(long amount, String currencyName)
    {
        if (amount < 0)
        {
            return new ConomyResponse(false, currencyName, this, amount, null, "Cannot give a negative amount!");
        }
        if (!this.doesSupport(currencyName))
        {
            return new ConomyResponse(false, currencyName, this, amount, null, "Unsupported currency!");
        }
        CurrencyAccount acc = accounts.get(currencyName);
        acc.setBalance(acc.getBalance() + amount);
        return new ConomyResponse(true, currencyName, this, amount, acc.getBalance(), null);
    }

    @Override
    public ConomyResponse take(long amount, String currencyName)
    {
        if (amount < 0)
        {
            return new ConomyResponse(false, currencyName, this, amount, null, "Cannot take a negative amount!");
        }
        if (!this.doesSupport(currencyName))
        {
            return new ConomyResponse(false, currencyName, this, amount, null, "Unsupported currency!");
        }
        CurrencyAccount acc = accounts.get(currencyName);
        acc.setBalance(acc.getBalance() - amount);
        return new ConomyResponse(true, currencyName, this, amount, acc.getBalance(), null);
    }

    @Override
    public ConomyResponse deposit(IAccount target, long amount, String currencyName)
    {
        if (amount < 0)
        {
            return new ConomyResponse(false, currencyName, this, amount, null, "Cannot deposit a negative amount!");
        }
        if (!this.doesSupport(currencyName))
        {
            return new ConomyResponse(false, currencyName, this, amount, null, "Unsupported currency!");
        }
        ConomyResponse response = target.give(amount, currencyName);
        if (response.success)
        {
            response = this.take(amount, currencyName);
            if (!response.success)
            {
                target.take(amount, currencyName);
            }
        }
        return response;
    }

    @Override
    public ConomyResponse withdraw(IAccount source, long amount, String currencyName)
    {
        if (amount < 0)
        {
            return new ConomyResponse(false, currencyName, this, amount, null, "Cannot deposit a negative amount!");
        }
        if (!this.doesSupport(currencyName))
        {
            return new ConomyResponse(false, currencyName, this, amount, null, "Unsupported currency!");
        }
        ConomyResponse response = source.take(amount, currencyName);
        if (response.success)
        {
            response = this.give(amount, currencyName);
            if (!response.success)
            {
                source.give(amount, currencyName);
            }
        }
        return response;
    }

    @Override
    public ConomyResponse balance(String currencyName)
    {
        if (!this.doesSupport(currencyName))
        {
            return new ConomyResponse(false, currencyName, this, null, null, "Unsupported currency!");
        }
        return new ConomyResponse(true, currencyName, this, null, this.accounts.get(currencyName).getBalance(), null);
    }

    @Override
    public ConomyResponse reset(String currencyname)
    {
        if (!this.doesSupport(currencyname))
        {
            return new ConomyResponse(false, currencyname, this, null, null, "Unsupported currency!");
        }
        this.accounts.get(currencyname).setBalance(0L);
        return new ConomyResponse(true, currencyname, this, null, 0L, null);
    }

    @Override
    public void resetAll()
    {
        for (String currency : this.accounts.keySet())
        {
            this.reset(currency);
        }
    }

    @Override
    public void resetAllToDefault()
    {
        for (String currency : this.accounts.keySet())
        {
            this.resetToDefault(currency);
        }
    }

    @Override
    public void resetToDefault(String currencyname)
    {
        this.accounts.get(currencyname).balance = 0L; //TODO get default
    }

    @Override
    public ConomyResponse set(long amount, String currencyName)
    {
        if (!this.doesSupport(currencyName))
        {
            return new ConomyResponse(false, currencyName, this, null, null, "Unsupported currency!");
        }
        this.accounts.get(currencyName).setBalance(amount);
        return new ConomyResponse(true, currencyName, this, null, amount, null);
    }

    @Override
    public ConomyResponse scale(double factor, String currencyName)
    {
        if (!this.doesSupport(currencyName))
        {
            return new ConomyResponse(false, currencyName, this, null, null, "Unsupported currency!");
        }
        CurrencyAccount acc = this.accounts.get(currencyName);
        long balance = acc.getBalance();
        balance *= factor;
        acc.setBalance(balance);
        return new ConomyResponse(true, currencyName, this, null, balance, null);
    }

    @Override
    public boolean doesSupport(String currencyName)
    {
        return this.accounts.keySet().contains(currencyName);
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

    @Override
    public boolean isUserAccount()
    {
        return user_id != null;
    }

    @Override
    public Collection<CurrencyAccount> getCurrencyAccounts()
    {
        return this.accounts.values();
    }

    @Override
    public CurrencyAccount getCurrencyAccount(String currencyName)
    {
        return this.accounts.get(currencyName);
    }
}
