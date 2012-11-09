package de.cubeisland.cubeengine.war.storage;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.groups.Group;
import de.cubeisland.cubeengine.war.groups.GroupControl;
import de.cubeisland.cubeengine.war.storage.UserModel.TeamPos;
import de.cubeisland.cubeengine.war.user.PlayerMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Anselm Brehme
 */
public class UserStorage implements Storage<User, UserModel>
{
    private final Database database;
    private final String TABLE = "user";
    private static UserStorage instance = null;

    public static UserStorage get()
    {
        if (instance == null)
        {
            instance = new UserStorage();
        }
        return instance;
    }

    public UserStorage()
    {
        this.database = CubeWar.getDB();

        try
        {
            this.database.prepareStatement("user_get", "SELECT * FROM {{" + TABLE + "}} WHERE cubeuserid=? LIMIT 1");
            this.database.prepareStatement("user_getall", "SELECT * FROM {{" + TABLE + "}}");
            this.database.prepareStatement("user_store", "INSERT INTO {{" + TABLE + "}} (cubeuserid,death,kills,kp,mode,teamid,teampos,influence) VALUES (?,?,?,?,?,?,?,?)");
            this.database.prepareStatement("user_delete", "DELETE FROM {{" + TABLE + "}} WHERE cubeuserid=?");
            this.database.prepareStatement("user_clear", "DELETE FROM {{" + TABLE + "}}");
            this.database.prepareStatement("user_update", "UPDATE {{" + TABLE + "}} SET death=?,kills=?,kp=?,mode=?,teamid=?,teampos=?,influence=? WHERE cubeuserid=?");

            this.initialize();
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
            this.database.exec("CREATE TABLE IF NOT EXISTS " + TABLE + "("
                    + "`cubeuserid` int(10) unsigned NOT NULL,"
                    + "`death` int(10) NOT NULL,"
                    + "`kills` int(20) NOT NULL,"
                    + "`kp` int(11) NOT NULL,"
                    + "`mode` int(2) NOT NULL,"
                    + "`teamid` int(4) NOT NULL,"
                    + "`teampos` int(3) NOT NULL,"
                    + "`influence` decimal(11,6) NOT NULL,"
                    + "PRIMARY KEY (`cubeuserid`)"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the User-Table !", ex);
        }
    }

    public Collection<UserModel> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery("user_getall");
            Collection<UserModel> users = new ArrayList<UserModel>();
            while (result.next())
            {
                int cubeuserid = result.getInt("cubeuserid");
                User cubeUser = CubeCore.getInstance().getUserManager().getUser(cubeuserid);
                int death = result.getInt("death");
                int kills = result.getInt("kills");
                int killpoints = result.getInt("kp");
                int modeInt = result.getInt("mode");
                int teamid = result.getInt("teamid");
                int teampos = result.getInt("teampos");//TODO TeamPos ist bei der Gruppe gespeichert
                TeamPos teamPos = TeamPos.NONE;
                switch (teampos)
                {
                    case 0:
                        teamPos = TeamPos.NONE;
                        break;
                    case 1:
                        teamPos = TeamPos.MEMBER;
                        break;
                    case 2:
                        teamPos = TeamPos.MODERATOR;
                        break;
                    case 3:
                        teamPos = TeamPos.ADMIN;
                        break;
                }
                Group team = GroupControl.get().getGroup(teamid);
                double influence = result.getDouble("influence");
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
                users.add(new UserModel(cubeUser, death, kills, killpoints, influence, mode, team, teamPos));
            }
            return users;


        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the users !", e);
        }
    }

    public void store(UserModel model)
    {
        try
        {

            int cubeuserid = model.getKey().getKey();
            int death = model.getDeath();
            int kills = model.getKills();
            int killpoints = model.getKillpoints();
            int teamid = model.getTeam().getKey();
            int modeInt = 0;
            TeamPos teamPos = model.getTeampos();
            int teampos = 0;
            switch (teamPos)
            {
                case NONE:
                    teampos = 0;
                    break;
                case MEMBER:
                    teampos = 1;
                    break;
                case MODERATOR:
                    teampos = 2;
                    break;
                case ADMIN:
                    teampos = 3;
                    break;
            }
            double influence = model.getInfluence();
            PlayerMode mode = model.getMode();
            switch (mode)
            {
                case NORMAL:
                    modeInt = 1;
                    break;
                case KILLRESET:
                    modeInt = 2;
                    break;
                case HIGHLANDER:
                    modeInt = 3;
                    break;
                case PEACE:
                    modeInt = 4;
                    break;
                case DUEL:
                    modeInt = 5;
                    break;
            }
            this.database.preparedExec("user_store", cubeuserid, death, kills, killpoints, modeInt, teamid, teampos, influence);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to store the user !", e);
        }
    }

    public void update(UserModel model)
    {
        try
        {
            int cubeuserid = model.getKey().getKey();
            int death = model.getDeath();
            int kills = model.getKills();
            int killpoints = model.getKillpoints();
            int teamid = model.getTeam().getKey();
            int modeInt = 0;
            TeamPos teamPos = model.getTeampos();
            int teampos = 0;
            switch (teamPos)
            {
                case NONE:
                    teampos = 0;
                    break;
                case MEMBER:
                    teampos = 1;
                    break;
                case MODERATOR:
                    teampos = 2;
                    break;
                case ADMIN:
                    teampos = 3;
                    break;
            }
            double influence = model.getInfluence();
            PlayerMode mode = model.getMode();
            switch (mode)
            {
                case NORMAL:
                    modeInt = 1;
                    break;
                case KILLRESET:
                    modeInt = 2;
                    break;
                case HIGHLANDER:
                    modeInt = 3;
                    break;
                case PEACE:
                    modeInt = 4;
                    break;
                case DUEL:
                    modeInt = 5;
                    break;
            }
            this.database.preparedExec("user_update", death, kills, killpoints, modeInt, teamid, teampos, influence, cubeuserid);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to update the user !", e);
        }
    }

    public boolean delete(UserModel model)
    {
        return this.delete(model.getKey());
    }

    public boolean delete(User key)
    {
        try
        {
            int id = key.getKey();
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

    public void merge(UserModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UserModel get(User key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
