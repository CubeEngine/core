package de.cubeisland.cubeengine.conomy.account.bank;

import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Faithcaio
 */
public class BankAccountStorage implements Storage<BankAccount>
{
    private final Database database;
    private final String TABLE = "auctions";

    public BankAccountStorage(Database database)
    {
        this.database = database;
        
        try
        {
            this.database.prepareStatement("bankacc_get",      "SELECT id,name,amount FROM {{"+ TABLE +"}} WHERE id=? LIMIT 1");
            this.database.prepareStatement("bankacc_getall",   "SELECT id,name,amount FROM {{"+ TABLE +"}}");
            this.database.prepareStatement("bankacc_store",    "INSERT INTO {{"+ TABLE +"}} (name,amount) VALUES (?,?)");
            this.database.prepareStatement("bankacc_update",   "UPDATE {{"+ TABLE +"}} SET amount=? WHERE id=?");
            this.database.prepareStatement("bankacc_merge",    "INSERT INTO {{"+ TABLE +"}} (id,amount) VALUES (?,?) ON DUPLICATE KEY UPDATE amount=values(amount)");
            this.database.prepareStatement("bankacc_delete",   "DELETE FROM {{"+ TABLE +"}} WHERE id=? LIMIT 1");
            this.database.prepareStatement("bankacc_clear",    "DELETE FROM {{"+ TABLE +"}}");
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
                "  `name` varchar(20) unsigned NOT NULL," +//TODO limit length
                "  `amount` decimal(11,2) NOT NULL," +
                "  PRIMARY KEY (`id`)" +
                ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;"
            );
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the UserTable !", ex);
        }
    }

    public BankAccount get(int key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("bankacc_get", key);

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
            ResultSet result = this.database.preparedQuery("bankacc_getall");

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
            PreparedStatement ps = this.database.getStatement("bankacc_store");
            ps.setString(1, account.getName());
            ps.setDouble(2, account.balance());
            this.database.assignId(ps,account);
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
            this.database.preparedUpdate("bankacc_update", account.balance(), account.getId());
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
            this.database.preparedUpdate("bankacc_merge", account.getId(), account.balance());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to merge the entry!", e);
        }
    }

    public boolean delete(BankAccount account)
    {
        return delete(account.getId());
    }

    public boolean delete(int id)
    {
        try
        {
            return this.database.preparedUpdate("usercc_delete", id) > 0;
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
            this.database.preparedExec("bankacc_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }
}
