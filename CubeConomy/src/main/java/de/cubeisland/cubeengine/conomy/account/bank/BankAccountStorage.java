package de.cubeisland.cubeengine.conomy.account.bank;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.Storage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.Database;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Anselm Brehme
 */
public class BankAccountStorage extends BasicStorage<BankAccount>
{
    public BankAccountStorage(Database database)
    {
        super(database, BankAccount.class);
    }

    public BankAccount get(Integer key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery(model,"get", key);

            if (!result.next())
            {
                return null;
            }
            
            int id = result.getInt("id");
            String name = result.getString("name");
            double amount = result.getDouble("amount");
            return new BankAccount(id, name, amount);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the acc '" + key + "'!", e);
        }
    }

    public Collection<BankAccount> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery(model,"getall");

            Collection<BankAccount> accs = new ArrayList<BankAccount>();
            
            int id;
            double amount;
            String name;
            while (result.next())
            {
                id = result.getInt("id");
                name = result.getString("name");
                amount = result.getDouble("amount");
                BankAccount acc = new BankAccount(id, name, amount);
                accs.add(acc);
            }
            return accs;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the accs from the database!", e);
        }
    }

    public void store(BankAccount account)
    {
        try
        {
            // TODO WTF?
            PreparedStatement ps = this.database.getStoredStatement(model,"store");
            ps.setString(1, account.getName());
            ps.setDouble(2, account.balance());
            //TODO give id  this.database.assignId(ps,account);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to store the BankAccount!", e);
        }
    }

    public void update(BankAccount account)
    {
        try
        {
            this.database.preparedUpdate(model,"update", account.balance(), account.getKey());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to update the entry!", e);
        }
    }

    public void merge(BankAccount account)
    {
        try
        {
            this.database.preparedUpdate(model,"merge", account.getKey(), account.balance());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to merge the entry!", e);
        }
    }

    public boolean delete(BankAccount account)
    {
        return delete(account.getKey());
    }

    public boolean delete(Integer id)
    {
        try
        {
            return this.database.preparedUpdate(model,"delete", id) > 0;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to delete the entry!", e);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExecute(model,"clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }

    public BankAccount get(Object key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean deleteByKey(Object key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
