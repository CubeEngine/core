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

public class AccountsManager extends SingleKeyStorage<Long, Account>
{
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
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the account-manager!", e);
        }
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
        return this.useraccounts.get(currency).get(user.key);
    }

    public boolean hasAccount(User user)
    {
        return this.hasAccount(user, this.getMainCurrency());
    }

    public boolean hasAccount(User user, Currency currency)
    {
        return this.useraccounts.get(currency).containsKey(user.key);
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

    public Account loadAccount(Long key)
    {
        try
        {
            Account loadedModel = null;
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getByUserID", key);
            if (resulsSet.next())
            {
                loadedModel = this.modelClass.newInstance();
                for (Field field : this.fieldNames.keySet())
                {
                    field.set(loadedModel, resulsSet.getObject(this.fieldNames.get(field)));
                }
                loadedModel.setCurrency(this.currencyManager);
            }
            return loadedModel;
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
}
