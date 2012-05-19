package de.cubeisland.cubeengine.war.area;

import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.database.AreaStorage;
import de.cubeisland.cubeengine.war.groups.Group_old;
import de.cubeisland.cubeengine.war.groups.GroupControl_old;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;

/**
 *
 * @author Faithcaio
 */
public class AreaControl_old implements Model
{

    private AreaStorage areaDB;
    private GroupControl_old groups = GroupControl_old.get();
    private THashMap<Chunk, Group_old> chunks = new THashMap<Chunk, Group_old>();

    public AreaControl_old()
    {
        areaDB = CubeWar.getInstance().getAreaDB();
    }

    public Group_old addChunk(Location loc, Group_old group)
    {
        return addChunk(loc.getChunk(), group);
    }

    public Group_old addChunk(Chunk chunk, Group_old group)
    {
        CubeWar.debug("ADD X: " + chunk.getX() + " Z:" + chunk.getZ() + " " + chunks.get(chunk) + " -->" + group);
        if (!(group.equals(chunks.get(chunk))))
        {
            if (chunks.get(chunk) == null)
            {
                group.addInfluence_used();
            }
            else
            {
                group.addInfluence_used();
                chunks.get(chunk).remInfluence_used();
            }
        }
        areaDB.store(group.getId(), chunk.getX(), chunk.getZ());
        return chunks.put(chunk, group);
    }

    public Group_old getGroup(Location loc)
    {
        return getGroup(loc.getChunk());
    }

    public Group_old getGroup(Chunk chunk)
    {
        Group_old tmp = chunks.get(chunk);
        if (tmp == null)
        {
            return groups.getWildLand();
        }
        return chunks.get(chunk);
    }

    public Group_old remChunk(Location loc)
    {
        return remChunk(loc.getChunk());
    }

    public Group_old remChunk(Chunk chunk)
    {
        CubeWar.debug("REM X: " + chunk.getX() + " Z:" + chunk.getZ() + " " + chunks.get(chunk));
        areaDB.delete(chunk.getX(), chunk.getZ());
        Group_old group = chunks.remove(chunk);
        if (group != null)
        {
            group.remInfluence_used();
        }
        return group;
    }

    public void remAll(Group_old group)
    {
        List<Chunk> remlist = new ArrayList<Chunk>();
        for (Chunk chunk : chunks.keySet())
        {
            if (chunks.get(chunk).equals(group))
            {
                remlist.add(chunk);
            }

        }
        for (Chunk chunk : remlist)
        {
            chunks.remove(chunk);
        }
        areaDB.deleteByGroup(group.getId());
        group.resetInfluence_used();
    }

    public void remAllAll()
    {
        chunks.clear();
        areaDB.clear();
    }

    public void load(Chunk chunk, int groupid)
    {
        Group_old group = groups.getGroup(groupid);
        chunks.put(chunk, group);
        group.addInfluence_used();
    }

    public int getId()
    {
        throw new UnsupportedOperationException("No Need");
    }
}
