package de.cubeisland.cubeengine.war.storage;

import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.cubeengine.war.groups.Group;
import org.bukkit.Chunk;
import org.bukkit.World;

/**
 *
 * @author Faithcaio
 */
public class AreaModel implements Model<Integer>
{
    private int key;
    
    private Chunk chunk;
    private Group group;
    
    public AreaModel(Chunk chunk, Group group)
    {
        this.chunk = chunk;
        this.group = group;
    }
    
    public Integer getKey()
    {
        return key;
    }
    
    public int getX()
    {
        return this.chunk.getX();
    }
    
    public int getZ()
    {
        return this.chunk.getZ();
    }
    
    public World getWorld()
    {
        return this.chunk.getWorld();
    }
    
    public Group getGroup()
    {
        return this.group;
    }
    
    public void setGroup(Group group)
    {
        this.group = group;
    }

    public Chunk getChunk()
    {
        return this.chunk;
    }
    
    public void setKey(Integer key)
    {
        this.key = key;
    }
    
}
