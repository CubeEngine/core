package de.cubeisland.CubeWar.Area;

import de.cubeisland.CubeWar.Groups.Group;
import de.cubeisland.CubeWar.Groups.GroupControl;
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
        //TODO Power change
        return chunks.put(loc.getChunk(), group);
    }
    
    public static Group addChunk(Chunk chunk, Group group)
    {
        return chunks.put(chunk, group);
    }
    
    public static Group getGroup(Location loc)
    {
        Group tmp = chunks.get(loc.getChunk());
        if (tmp == null)
            return GroupControl.getWildLand();
        return chunks.get(loc.getChunk());
    }
    
    public static Group remChunk(Location loc)
    {
        return chunks.remove(loc.getChunk());
    }
    
    public static Group remChunk(Chunk chunk)
    {
        return chunks.remove(chunk);
    }
    
    public static void remAll(Group group)
    {
        for (Chunk chunk : chunks.keySet())
        {
            if (chunks.get(chunk).equals(group))
                chunks.remove(chunk);
        }
                    
    }
    
    public static void remAllAll()
    {
        chunks.clear();
    }
}
