package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.Conomy;
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
    private THashMap<String, Account> bankaccounts = new THashMap<String, Account>();
    private TLongObjectHashMap<Account> useraccounts = new TLongObjectHashMap<Account>();
    private Conomy module;

    public AccountsManager(Conomy module)
    {
        super(module.getDatabase(), Account.class, REVISION);
        this.module = module;
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
                    builder.select().cols(allFields).from(this.tableName).where()
                    .field("user_id").isEqual().value().end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the account-manager!", e);
        }
    }

    public Account getAccount(User user)
    {
        return this.useraccounts.get(user.key);
    }

    public boolean hasAccount(User user)
    {
        if (this.useraccounts.containsKey(user.key))
        {
            return true;
        }
        Account acc = this.loadAccount(user.key);
        if (acc == null)
        {
            return false;
        }
        acc.loadInCurrencyAccounts(this.module.getCurrencyAccountManager().getAccounts(acc));
        this.useraccounts.put(user.key, acc);
        return true;
    }

    public Account createNewAccount(User user)
    {
        Account acc = new Account(user);
        this.store(acc);
        //TODO permission check for the currencies
        acc.loadInCurrencyAccounts(this.module.getCurrencyAccountManager().createAccounts(acc));
        this.useraccounts.put(user.key, acc);
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
