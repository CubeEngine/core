package de.cubeisland.cubeengine.war.storage;

import org.bukkit.Chunk;

/**
 *
 * @author Faithcaio
 */
public class Area
{
    AreaModel model;
    
    public Area(Chunk chunk, Group group)
    {
        this.model = new AreaModel(chunk, group);
    }
}
