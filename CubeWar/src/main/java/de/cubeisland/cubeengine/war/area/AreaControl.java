package de.cubeisland.cubeengine.war.area;

import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.database.AreaStorage;
import de.cubeisland.cubeengine.war.groups.Group;
import de.cubeisland.cubeengine.war.groups.GroupControl;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;

/**
 *
 * @author Faithcaio
 */
public class AreaControl implements Model
{

    private AreaStorage areaDB;
    private GroupControl groups = GroupControl.get();
    private THashMap<Chunk, Group> chunks = new THashMap<Chunk, Group>();

    public AreaControl()
    {
        areaDB = CubeWar.getInstance().getAreaDB();
    }

    public Group addChunk(Location loc, Group group)
    {
        return addChunk(loc.getChunk(), group);
    }

    public Group addChunk(Chunk chunk, Group group)
    {
        CubeWar.debug("ADD X: " + chunk.getX() + " Z:" + chunk.getZ() + " " + chunks.get(chunk) + " -->" + group);
        if (!(group.equals(chunks.get(chunk))))
        {
            if (chunks.get(chunk) == null)
            {
                group.addPower_used();
            }
            else
            {
                group.addPower_used();
                chunks.get(chunk).remPower_used();
            }
        }
        areaDB.store(group.getId(), chunk.getX(), chunk.getZ());
        return chunks.put(chunk, group);
    }

    public Group getGroup(Location loc)
    {
        return getGroup(loc.getChunk());
    }

    public Group getGroup(Chunk chunk)
    {
        Group tmp = chunks.get(chunk);
        if (tmp == null)
        {
            return groups.getWildLand();
        }
        return chunks.get(chunk);
    }

    public Group remChunk(Location loc)
    {
        return remChunk(loc.getChunk());
    }

    public Group remChunk(Chunk chunk)
    {
        CubeWar.debug("REM X: " + chunk.getX() + " Z:" + chunk.getZ() + " " + chunks.get(chunk));
        areaDB.delete(chunk.getX(), chunk.getZ());
        Group group = chunks.remove(chunk);
        if (group != null)
        {
            group.remPower_used();
        }
        return group;
    }

    public void remAll(Group group)
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
        group.resetPower_used();
    }

    public void remAllAll()
    {
        chunks.clear();
        areaDB.clear();
    }

    public void load(Chunk chunk, int groupid)
    {
        chunks.put(chunk, groups.getGroup(groupid));
    }

    public int getId()
    {
        throw new UnsupportedOperationException("No Need");
    }
}
