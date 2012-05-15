/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
 * @author Faithcaio
 */
public class GroupStorage implements Storage<Group>{

    private final Database database = CubeWar.getDB();
    private final String TABLE = "groups";
    
    public GroupStorage() 
    {
        this.initialize();
        try
        {//TODO noch alles falsch hier...
            this.database.prepareStatement("group_get", "SELECT id,cubeuserid,item,amount,timestamp FROM {{" + TABLE + "}} WHERE id=? LIMIT 1");
            this.database.prepareStatement("group_getall", "SELECT id,cubeuserid,item,amount,timestamp FROM {{" + TABLE + "}}");
            this.database.prepareStatement("group_store", "INSERT INTO {{" + TABLE + "}} (id,cubeuserid,item,amount,timestamp) VALUES (?,?,?,?,?)");
            this.database.prepareStatement("group_delete", "DELETE FROM {{" + TABLE + "}} WHERE id=?");
            this.database.prepareStatement("group_clear", "DELETE FROM {{" + TABLE + "}}");
            //this.database.prepareStatement("auction_update",   "UPDATE {{"+TABLE+"}} SET flags=? WHERE id=?");
            //this.database.prepareStatement("auction_merge",    "INSERT INTO {{"+TABLE+"}} (name,flags) VALUES (?,?) ON DUPLICATE KEY UPDATE flags=values(flags)");
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

    public Group get(int key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Group> getAll()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void store(Group model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(Group model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(Group model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(Group model)
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
