package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.conomy.currency.CurrencyManager;
import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.user.User;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class AccountsManager extends SingleKeyStorage<Long, Account>
{//TODO custom queries for money top
    private static final int REVISION = 1;
    private THashMap<Currency, THashMap<String, Account>> bankaccounts = new THashMap<Currency, THashMap<String, Account>>();
    private THashMap<Currency, TLongObjectHashMap<Account>> useraccounts = new THashMap<Currency, TLongObjectHashMap<Account>>();
    private Conomy module;
    private CurrencyManager currencyManager;

    public AccountsManager(Conomy module)
    {
        super(module.getDatabase(), Account.class, REVISION);
        this.module = module;
        this.currencyManager = module.getCurrencyManager();
        for (Currency currency : this.currencyManager.getAllCurrencies())
        {
            this.useraccounts.put(currency, new TLongObjectHashMap<Account>());
        }
        this.initialize();
    }

    @Override
    public void initialize()
    {
        try
        {
            super.initialize();
            QueryBuilder builder = this.database.getQueryBuilder();
            this.database.storeStatement(modelClass, "getByUserID",
                    builder.select().cols(allFields).from(this.tableName).
                    where().field("user_id").isEqual().value().end().end());
            this.database.storeStatement(modelClass, "getByAccountName",
                    builder.select().cols(allFields).from(this.tableName).
                    where().field("name").isEqual().value().end().end());
            this.database.storeStatement(modelClass, "getTopBalance",
                    builder.select().cols(allFields).from(this.tableName).
                    where().field("currencyName").isEqual().value().
                    orderBy("value").desc().limit().offset().end().end());

            
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the account-manager!", e);
        }
    }

    @Override
    public void store(Account model)
    {
        if (model.value <0)
        {
            model.value *= -1;
            model.positive = false;
        }
        else
        {
            model.positive = true;
        }
        super.store(model);
    }

    @Override
    public void update(Account model)
    {
        if (model.value <0)
        {
            model.value *= -1;
            model.positive = false;
        }
        else
        {
            model.positive = true;
        }
        super.update(model);
    }
    
    

    public Account getAccount(User user)
    {
        return this.getAccount(user, this.module.getCurrencyManager().getMainCurrency());
    }

    public Currency getMainCurrency()
    {
        return this.module.getCurrencyManager().getMainCurrency();
    }

    public Account getAccount(User user, Currency currency)
    {
        this.hasAccount(user, currency); //loads accounts if not yet loaded
        return this.useraccounts.get(currency).get(user.key);
    }

    public Account getAccount(String name, Currency currency)
    {
        this.hasAccount(name, currency);
        return this.bankaccounts.get(currency).get(name);

    }

    public boolean hasAccount(String name, Currency currency)
    {
        boolean found = this.bankaccounts.get(currency).containsKey(name);
        if (!found)
        {
            this.loadAccount(name);
            found = this.bankaccounts.get(currency).containsKey(name);
        }
        return found;
    }

    public boolean hasAccount(User user)
    {
        return this.hasAccount(user, this.getMainCurrency());
    }

    public boolean hasAccount(User user, Currency currency)
    {
        boolean found = this.useraccounts.get(currency).containsKey(user.key);
        if (!found)
        {
            this.loadAccount(user.key);
            found = this.useraccounts.get(currency).containsKey(user.key);
        }
        return found;
    }

    public Account createNewAccount(User user)
    {
        return this.createNewAccount(user, this.getMainCurrency());
    }

    public Account createNewAccount(User user, Currency currency)
    {
        Account acc = new Account(currency, user);
        this.store(acc);
        this.useraccounts.get(currency).put(user.key, acc);
        return acc;
    }

    public void loadAccount(Long key)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getByUserID", key);
            while (resulsSet.next())
            {
                Account loadedModel = this.modelClass.newInstance();
                for (Field field : this.fieldNames.keySet())
                {
                    field.set(loadedModel, resulsSet.getObject(this.fieldNames.get(field)));
                }
                loadedModel.init(this.currencyManager);
                this.useraccounts.get(loadedModel.currency).put(key, loadedModel);
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while reading from Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
        }
    }

    public void loadAccount(String name)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getByAccountName", name);
            while (resulsSet.next())
            {
                Account loadedModel = this.modelClass.newInstance();
                for (Field field : this.fieldNames.keySet())
                {
                    field.set(loadedModel, resulsSet.getObject(this.fieldNames.get(field)));
                }
                loadedModel.init(this.currencyManager);
                this.bankaccounts.get(loadedModel.currency).put(name, loadedModel);
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while reading from Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
        }
    }

    public Collection<Account> getAccounts(User user)
    {
        ArrayList<Account> result = new ArrayList<Account>();
        for (Currency currency : this.module.getCurrencyManager().getAllCurrencies())
        {
            Account acc = this.useraccounts.get(currency).get(user.key);
            if (acc != null)
            {
                result.add(acc);
            }
        }
        return result;
    }

    public Collection<Account> getTopAccounts(Currency currency, int fromRank, int toRank)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getTopBalance", currency.getName(), toRank - fromRank, fromRank - 1);
            LinkedList<Account> list = new LinkedList<Account>();
            while (resulsSet.next())
            {
                Account loadedModel = this.modelClass.newInstance();
                for (Field field : this.fieldNames.keySet())
                {
                    field.set(loadedModel, resulsSet.getObject(this.fieldNames.get(field)));
                }
                loadedModel.init(this.currencyManager);
                list.add(loadedModel);
            }
            return list;
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while reading from Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
        }
    }

    public Collection<Account> getTopAccounts(int fromRank, int toRank)
    {
        return this.getTopAccounts(this.getMainCurrency(), fromRank, toRank);
    }

    public boolean transaction(Account source, Account target, long amount, boolean force)
    { 
        if (source.value - amount < 0)
        {
            //TODO check if source has enough money OR perm for negative / below kreditlimit
            //TODO add low/limit
        }
        if (!force && amount < 0)
        {
            throw new IllegalArgumentException("Transactions with negative amount are not allowed unless forced!");
        }
        target.transaction(source, amount);
        this.update(target);
        this.update(source);
        return true;
    }
}
