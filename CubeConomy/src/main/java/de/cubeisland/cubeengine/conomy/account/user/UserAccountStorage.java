package de.cubeisland.cubeengine.conomy.account.user;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Anselm Brehme
 */
public class UserAccountStorage extends BasicStorage<UserAccount>
{
    private final UserManager cuManager;

    public UserAccountStorage(Database database, UserManager cuManager)
    {
        super(database, UserAccount.class);
        this.cuManager = cuManager;
    }

    public UserAccount get(Object key)
    {
        try
        {

            ResultSet result = this.database.preparedQuery(model, "get", key);
            if (!result.next())
            {
                return null;
            }
            User user = this.cuManager.getUser((Integer) key);
            double amount = result.getDouble("amount");
            return new UserAccount(user, amount);

        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the acc '" + key + "'!", e);
        }
    }

    public Collection<UserAccount> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery(model, "getall");

            Collection<UserAccount> accs = new ArrayList<UserAccount>();

            int key;
            double amount;
            User user;
            while (result.next())
            {
                key = result.getInt("id");
                user = this.cuManager.getUser(key);
                amount = result.getDouble("amount");
                UserAccount acc = new UserAccount(user, amount);
                accs.add(acc);
            }

            return accs;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the accs from the database!", e);
        }
    }

    public void store(UserAccount account)
    {
        try
        {
            this.database.preparedExecute(model, "store", account.getKey(), account.balance());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to store the UserAccount!", e);
        }
    }

    public void update(UserAccount account)
    {
        try
        {
            this.database.preparedUpdate(model, "update", account.balance(), account.getKey());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to update the entry!", e);
        }
    }

    public void merge(UserAccount account)
    {
        try
        {
            this.database.preparedUpdate(model, "merge", account.getKey(), account.balance());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to merge the entry!", e);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExecute(model, "clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }

    public boolean deleteByKey(Object key)
    {
        try
        {
            return this.database.preparedUpdate(model, "delete", key) > 0;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to delete the entry!", e);
        }
    }

    public boolean delete(UserAccount account)
    {
        return deleteByKey(account.getKey());
    }
}