package de.cubeisland.cubeengine.war.database;

import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.user.User;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author Anselm
 */
public class UserStorage implements Storage<User>{

    private final Database database = CubeWar.getDB();
    private final String TABLE = "user";
    
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
                + "PRIMARY KEY (`cubeuserid`),"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the User-Table !", ex);
        }
    }

    public User get(int key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<User> getAll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void store(User model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(User model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(User model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(User model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
