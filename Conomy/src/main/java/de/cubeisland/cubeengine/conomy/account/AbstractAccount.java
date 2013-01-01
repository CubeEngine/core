package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.user.User;
import gnu.trove.map.hash.THashMap;

public class AbstractAccount implements IAccount, Model<Long>
{
    private long key = -1;
    
    private User user;
    private String name;
    //TODO currency limits in config too (e.g. min money set to 0 )
    private THashMap<Currency, Account> accounts = new THashMap<Currency, Account>();

    @Override
    public ConomyResponse give(long amount, Currency currency)
    {
        if (amount < 0)
        {
            return new ConomyResponse(false, currency, this, amount, null, "Cannot give a negative amount!");
        }
        if (!this.doesSupport(currency))
        {
            return new ConomyResponse(false, currency, this, amount, null, "Unsupported currency!");
        }
        Account acc = accounts.get(currency);
        acc.setBalance(acc.getBalance() + amount);
        return new ConomyResponse(true, currency, this, amount, acc.getBalance(), null);
    }

    @Override
    public ConomyResponse take(long amount, Currency currency)
    {
        if (amount < 0)
        {
            return new ConomyResponse(false, currency, this, amount, null, "Cannot take a negative amount!");
        }
        if (!this.doesSupport(currency))
        {
            return new ConomyResponse(false, currency, this, amount, null, "Unsupported currency!");
        }
        Account acc = accounts.get(currency);
        acc.setBalance(acc.getBalance() - amount);
        return new ConomyResponse(true, currency, this, amount, acc.getBalance(), null);
    }

    @Override
    public ConomyResponse deposit(IAccount target, long amount, Currency currency)
    {
        if (amount < 0)
        {
            return new ConomyResponse(false, currency, this, amount, null, "Cannot deposit a negative amount!");
        }
        if (!this.doesSupport(currency))
        {
            return new ConomyResponse(false, currency, this, amount, null, "Unsupported currency!");
        }
        ConomyResponse response = target.give(amount, currency);
        if (response.success)
        {
            response = this.take(amount, currency);
            if (!response.success)
            {
                target.take(amount, currency);
            }
        }
        return response;
    }

    @Override
    public ConomyResponse withdraw(IAccount source, long amount, Currency currency)
    {
        if (amount < 0)
        {
            return new ConomyResponse(false, currency, this, amount, null, "Cannot deposit a negative amount!");
        }
        if (!this.doesSupport(currency))
        {
            return new ConomyResponse(false, currency, this, amount, null, "Unsupported currency!");
        }
        ConomyResponse response = source.take(amount, currency);
        if (response.success)
        {
            response = this.give(amount, currency);
            if (!response.success)
            {
                source.give(amount, currency);
            }
        }
        return response;
    }

    @Override
    public ConomyResponse balance(Currency currency)
    {
        if (!this.doesSupport(currency))
        {
            return new ConomyResponse(false, currency, this, null, null, "Unsupported currency!");
        }
        return new ConomyResponse(true, currency, this, null, this.accounts.get(currency).getBalance(), null);
    }

    @Override
    public ConomyResponse reset(Currency currency)
    {
        if (!this.doesSupport(currency))
        {
            return new ConomyResponse(false, currency, this, null, null, "Unsupported currency!");
        }
        this.accounts.get(currency).setBalance(0L);
        return new ConomyResponse(true, currency, this, null, 0L, null);
    }

    @Override
    public void resetAll()
    {
        for (Currency currency : this.accounts.keySet())
        {
            this.reset(currency);
        }
    }

    @Override
    public ConomyResponse set(long amount, Currency currency)
    {
        if (!this.doesSupport(currency))
        {
            return new ConomyResponse(false, currency, this, null, null, "Unsupported currency!");
        }
        this.accounts.get(currency).setBalance(amount);
        return new ConomyResponse(true, currency, this, null, amount, null);
    }

    @Override
    public ConomyResponse scale(double factor, Currency currency)
    {
        if (!this.doesSupport(currency))
        {
            return new ConomyResponse(false, currency, this, null, null, "Unsupported currency!");
        }
        Account acc = this.accounts.get(currency);
        long balance = acc.getBalance();
        balance *= factor;
        acc.setBalance(balance);
        return new ConomyResponse(true, currency, this, null, balance, null);
    }

    @Override
    public boolean doesSupport(Currency currency)
    {
        return this.accounts.keySet().contains(currency);
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
        return user != null;
    }
    
    
}
