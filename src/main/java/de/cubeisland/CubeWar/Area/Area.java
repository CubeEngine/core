package de.cubeisland.CubeWar.Area;

import de.cubeisland.CubeWar.Groups.Group;
import gnu.trove.map.hash.THashMap;
import org.bukkit.Chunk;
import org.bukkit.Location;

/**
 *
 * @author Faithcaio
 */
public class Area {

    private static THashMap<Chunk,Group> chunks = new THashMap<Chunk,Group>();
    
    public Area() 
    {
    
    }
    
    public static Group addChunk(Location loc, Group group)
    {
        return chunks.put(loc.getChunk(), group);
    }
    
    public static Group addChunk(Chunk chunk, Group group)
    {
        return chunks.put(chunk, group);
    }
    
    public static Group getGroup(Location loc)
    {
        return chunks.get(loc.getChunk());
    }
    
    public static Group remChunk(Location loc)
    {
        return chunks.remove(loc.getChunk());
    }
}
