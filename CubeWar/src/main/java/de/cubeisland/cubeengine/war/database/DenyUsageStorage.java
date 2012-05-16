package de.cubeisland.cubeengine.war.database;

import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.groups.Group;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author Anselm
 */
public class DenyUsageStorage implements Storage<Group>{

    private final Database database = CubeWar.getDB();
    private final String TABLE = "denyusage";
    
    public void initialize() 
    {
        try
        {
            this.database.exec("CREATE TABLE IF NOT EXISTS `denyusage` ("
                + "`groupid` int(10) unsigned NOT NULL,"
                + "`deny` String(42) NOT NULL,"
                + "PRIMARY KEY (`groupid`),"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the UsageDeny-Table !", ex);
        }
    }

    public Group get(int key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Group> getAll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void store(Group model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(Group model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(Group model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(Group model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
   //TODO deniedCmd / protections
}
