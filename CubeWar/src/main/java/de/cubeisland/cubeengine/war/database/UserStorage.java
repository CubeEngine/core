package de.cubeisland.cubeengine.war.database;

import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.user.PlayerMode;
import de.cubeisland.cubeengine.war.user.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Anselm
 */
public class UserStorage implements Storage<User>
{

    private final Database database = CubeWar.getDB();
    private final String TABLE = "user";

    public UserStorage()
    {
        this.initialize();
        try
        {
            this.database.prepareStatement("user_get", "SELECT cubeuserid,death,kills,kp,mode,teamid {{" + TABLE + "}} WHERE cubeuserid=? LIMIT 1");
            this.database.prepareStatement("user_getall", "SELECT cubeuserid,death,kills,kp,mode,teamid FROM {{" + TABLE + "}}");
            this.database.prepareStatement("user_store", "INSERT INTO {{" + TABLE + "}} (cubeuserid,death,kills,kp,mode,teamid) VALUES (?,?,?,?,?,?)");
            this.database.prepareStatement("user_delete", "DELETE FROM {{" + TABLE + "}} WHERE cubeuserid=?");
            this.database.prepareStatement("user_clear", "DELETE FROM {{" + TABLE + "}}");
            this.database.prepareStatement("user_update", "UPDATE {{" + TABLE + "}} SET death=?,kills=?,kp=?,mode=?,teamid=? WHERE cubeuserid=?");
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
            this.database.exec("CREATE TABLE IF NOT EXISTS `user` ("
                    + "`cubeuserid` int(10) unsigned NOT NULL,"
                    + "`death` int(10) NOT NULL,"
                    + "`kills` int(20) NOT NULL,"
                    + "`kp` int(11) NOT NULL,"
                    + "`mode` int(2) NOT NULL,"
                    + "`teamid` int(4) NOT NULL,"
                    + "PRIMARY KEY (`cubeuserid`)"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the User-Table !", ex);
        }
    }

    public User get(int key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("user_get", key);

            if (!result.next())
            {
                return null;
            }
            int cubeuserid = result.getInt("cubeuserid");
            int death = result.getInt("death");
            int kills = result.getInt("kills");
            int kp = result.getInt("kp");
            int modeInt = result.getInt("mode");
            int teamid = result.getInt("teamid");
            PlayerMode mode = null;
            switch (modeInt)
            {
                case 1:
                    mode = PlayerMode.NORMAL;
                    break;
                case 2:
                    mode = PlayerMode.KILLRESET;
                    break;
                case 3:
                    mode = PlayerMode.HIGHLANDER;
                    break;
                case 4:
                    mode = PlayerMode.PEACE;
                    break;
                case 5:
                    mode = PlayerMode.DUEL;
                    break;
            }

            return new User(cubeuserid, death, kills, kp, mode, teamid);

        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the user '" + key + "'!", e);
        }
    }

    public Collection<User> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery("user_getall");
            Collection<User> users = new ArrayList<User>();
            while (result.next())
            {
                int cubeuserid = result.getInt("cubeuserid");
                int death = result.getInt("death");
                int kills = result.getInt("kills");
                int kp = result.getInt("kp");
                int modeInt = result.getInt("mode");
                int teamid = result.getInt("teamid");
                PlayerMode mode = null;
                switch (modeInt)
                {
                    case 1:
                        mode = PlayerMode.NORMAL;
                        break;
                    case 2:
                        mode = PlayerMode.KILLRESET;
                        break;
                    case 3:
                        mode = PlayerMode.HIGHLANDER;
                        break;
                    case 4:
                        mode = PlayerMode.PEACE;
                        break;
                    case 5:
                        mode = PlayerMode.DUEL;
                        break;
                }
                users.add(new User(cubeuserid, death, kills, kp, mode, teamid));
            }
            return users;


        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the users !", e);
        }
    }

    public void store(User model)
    {
        try
        {

            int cubeuserid = model.getId();
            int death = model.getDeath();
            int kills = model.getKills();
            int kp = model.getKp();
            int teamid = model.getTeam().getId();
            int modeInt = 0;
            PlayerMode mode = model.getMode();
            if (mode.equals(PlayerMode.NORMAL))
            {
                modeInt = 1;
            }
            if (mode.equals(PlayerMode.KILLRESET))
            {
                modeInt = 2;
            }
            if (mode.equals(PlayerMode.HIGHLANDER))
            {
                modeInt = 3;
            }
            if (mode.equals(PlayerMode.PEACE))
            {
                modeInt = 4;
            }
            if (mode.equals(PlayerMode.DUEL))
            {
                modeInt = 5;
            }
            this.database.preparedExec("user_store", cubeuserid, death, kills, kp, modeInt, teamid);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to store the user !", e);
        }
    }

    public boolean delete(User model)
    {
        return this.delete(model.getId());
    }

    public boolean delete(int id)
    {
        try
        {
            return this.database.preparedExec("group_delete", id);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to delete the user", e);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExec("user_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }

    public void merge(User model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(User model)
    {
        try
        {
            int cubeuserid = model.getId();
            int death = model.getDeath();
            int kills = model.getKills();
            int kp = model.getKp();
            int teamid = model.getTeam().getId();
            int modeInt = 0;
            PlayerMode mode = model.getMode();
            if (mode.equals(PlayerMode.NORMAL))
            {
                modeInt = 1;
            }
            if (mode.equals(PlayerMode.KILLRESET))
            {
                modeInt = 2;
            }
            if (mode.equals(PlayerMode.HIGHLANDER))
            {
                modeInt = 3;
            }
            if (mode.equals(PlayerMode.PEACE))
            {
                modeInt = 4;
            }
            if (mode.equals(PlayerMode.DUEL))
            {
                modeInt = 5;
            }
            this.database.preparedExec("user_update", death, kills, kp, modeInt, teamid, cubeuserid);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to update the user !", e);
        }
    }
}
