package de.cubeisland.cubeengine.war.storage;

import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.war.CubeWar;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author Anselm
 */
public class UserStorage implements Storage<UserModel>
{
    private final Database database = CubeWar.getDB();
    private final String TABLE = "user";

    public UserStorage()
    {
        this.initialize();
        try
        {
            this.database.prepareStatement("user_get", "SELECT * FROM {{" + TABLE + "}} WHERE cubeuserid=? LIMIT 1");
            this.database.prepareStatement("user_getall", "SELECT * FROM {{" + TABLE + "}}");
            this.database.prepareStatement("user_store", "INSERT INTO {{" + TABLE + "}} (cubeuserid,death,kills,kp,mode,teamid,teampos,influence) VALUES (?,?,?,?,?,?,?,?)");
            this.database.prepareStatement("user_delete", "DELETE FROM {{" + TABLE + "}} WHERE cubeuserid=?");
            this.database.prepareStatement("user_clear", "DELETE FROM {{" + TABLE + "}}");
            this.database.prepareStatement("user_update", "UPDATE {{" + TABLE + "}} SET death=?,kills=?,kp=?,mode=?,teamid=?,teampos=?,influence=? WHERE cubeuserid=?");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to prepare the statements!", e);
        }
    }
    
    public void initialize()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UserModel get(int key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<UserModel> getAll()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void store(UserModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(UserModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(UserModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(UserModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(int id)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
