package de.cubeisland.cubeengine.war.database;

import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.area.Area;
import java.sql.SQLException;
import java.util.Collection;
import org.bukkit.Server;

/**
 *
 * @author Anselm
 */
public class AreaStorage implements Storage<Area>{

    private final Database database = CubeWar.getDB();
    private final String TABLE = "area";
    private Server server = CubeWar.getInstance().getServer();
    //TODO add multiverse support
    
    public AreaStorage() 
    {
        this.initialize();
        try
        {
            this.database.prepareStatement("group_get", "SELECT groupid {{" + TABLE + "}} WHERE x=? && z=? LIMIT 1");
            this.database.prepareStatement("group_getall", "SELECT groupid,x,z FROM {{" + TABLE + "}}");
            this.database.prepareStatement("group_store", "INSERT INTO {{" + TABLE + "}} (groupid,x,z) VALUES (?,?,?)");
            this.database.prepareStatement("group_delete", "DELETE FROM {{" + TABLE + "}} WHERE x=? && z=?");
            this.database.prepareStatement("group_clear", "DELETE FROM {{" + TABLE + "}}");
            this.database.prepareStatement("group_update",   "UPDATE {{"+TABLE+"}} SET "+
                                                        "groupid=? WHERE x=? && z=?");
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
            this.database.exec("CREATE TABLE IF NOT EXISTS `area` ("
                + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                + "`groupid` int(11) NOT NULL,"
                + "`x` int(10) NOT NULL,"
                + "`z` int(11) NOT NULL,"
                + "PRIMARY KEY (`id`),"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the UsageDeny-Table !", ex);
        }
    }

    
    /**
     * 
     * @param x
     * @param z
     * @return GroupId of this Chunk
     */
    public int get(int x, int z) 
    {
        return 0;//TODO
    }
    
    /**
     * 
     * @return Area filled with all Areas from DB
     */
    public Area load()
    {
        Area area = new Area();
        return area;//TODO
    }
    
    public void store (int groupid, int x, int z)
    {
        
    }

    
    public Area get(int key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Collection<Area> getAll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void store(Area model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(Area model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(Area model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(Area model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //TODO Store 
}
