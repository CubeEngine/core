package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CurrencyAccountManager extends SingleKeyStorage<Long, CurrencyAccount>
{
    private static final int REVISION = 1;
    private Conomy module;

    public CurrencyAccountManager(Conomy module)
    {
        super(module.getDatabase(), CurrencyAccount.class, REVISION);
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
            this.database.storeStatement(modelClass, "getByAccId",
                    builder.select().cols(allFields).from(this.tableName).where()
                    .field("account_id").isEqual().value().end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the currency-account-manager!", e);
        }
    }

    public Collection<CurrencyAccount> createAccounts(Account acc)
    {
        List<CurrencyAccount> currencyAccounts = new ArrayList<CurrencyAccount>();
        for (Currency currency : this.module.getCurrencyManager().getAllCurrencies())
        {
            CurrencyAccount currencyAccount = new CurrencyAccount(currency, acc);
            this.store(currencyAccount);
            currencyAccounts.add(currencyAccount);
        }
        return currencyAccounts;
    }

    public Collection<CurrencyAccount> getAccounts(Account acc)
    {
        try
        {
            List<CurrencyAccount> loadedModels = new ArrayList<CurrencyAccount>();
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getByAccId", acc.key);
            while (resulsSet.next())
            {
                CurrencyAccount loadedModel = this.modelClass.newInstance();
                for (Field field : this.fieldNames.keySet())
                {
                    field.set(loadedModel, resulsSet.getObject(this.fieldNames.get(field)));
                }
                loadedModels.add(loadedModel);
            }
            return loadedModels;
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
