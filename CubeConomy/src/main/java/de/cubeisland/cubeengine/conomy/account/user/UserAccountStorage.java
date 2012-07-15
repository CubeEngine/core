package de.cubeisland.cubeengine.conomy.account.user;

import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.persistence.database.Database;
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
public class UserAccountStorage implements Storage<Integer, UserAccount>
{
    private final Database database;
    private final UserManager cuManager;
    private final String TABLE = "auctions";

    public UserAccountStorage(UserManager cuManager, Database database)
    {
        this.database = database;
        this.cuManager = cuManager;
        
        try
        {
            this.database.prepareStatement("useracc_get",      "SELECT id,amount FROM {{"+ TABLE +"}} WHERE id=? LIMIT 1");
            this.database.prepareStatement("useracc_getall",   "SELECT id,amount FROM {{"+ TABLE +"}}");
            this.database.prepareStatement("useracc_store",    "INSERT INTO {{"+ TABLE +"}} (id,amount) VALUES (?,?)");
            this.database.prepareStatement("useracc_update",   "UPDATE {{"+ TABLE +"}} SET amount=? WHERE id=?");
            this.database.prepareStatement("useracc_merge",    "INSERT INTO {{"+ TABLE +"}} (id,amount) VALUES (?,?) ON DUPLICATE KEY UPDATE amount=values(amount)");
            this.database.prepareStatement("useracc_delete",   "DELETE FROM {{"+ TABLE +"}} WHERE id=? LIMIT 1");
            this.database.prepareStatement("useracc_clear",    "DELETE FROM {{"+ TABLE +"}}");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to prepare the statements!", e);
        }
    }
    
    public void initialize()
    {
        try
        {
            this.database.exec(
                "CREATE TABLE IF NOT EXISTS `{{"+ TABLE +"}}` (" +
                "  `id` int(11) unsigned NOT NULL," +
                "  `amount` decimal(11,2) NOT NULL," +
                "  PRIMARY KEY (`id`)" +
                // "FOREIGN KEY (`id`) REFERENCES core_user(id)" +
                ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;"
            );
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the UserAcc-Table !", ex);
        }
    }

    public UserAccount get(Integer key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("useracc_get", key);

            if (!result.next())
            {
                return null;
            }
            User user = this.cuManager.getUser(key);
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
            ResultSet result = this.database.preparedQuery("useracc_getall");

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
            this.database.preparedExec("useracc_store",account.getKey(),account.balance());
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
            this.database.preparedUpdate("useracc_update", account.balance(), account.getKey());
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
            this.database.preparedUpdate("useracc_merge", account.getKey(), account.balance());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to merge the entry!", e);
        }
    }

    public boolean delete(UserAccount account)
    {
        return delete(account.getKey());
    }

    public boolean delete(Integer key)
    {
        try
        {
            return this.database.preparedUpdate("usercc_delete", key) > 0;
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
            this.database.preparedExec("useracc_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }
}
