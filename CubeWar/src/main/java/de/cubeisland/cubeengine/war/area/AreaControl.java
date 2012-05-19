package de.cubeisland.cubeengine.war.area;

import de.cubeisland.cubeengine.war.storage.AreaModel;
import de.cubeisland.cubeengine.war.storage.AreaStorage;
import de.cubeisland.cubeengine.war.groups.Group;
import de.cubeisland.cubeengine.war.groups.GroupControl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;

/**
 *
 * @author Faithcaio
 */
public class AreaControl
{

    private AreaStorage areaDB;
    private HashMap<Chunk, Area> areas = new HashMap<Chunk, Area>();
    private static AreaControl instance = null;
    
    public AreaControl()
    {
        areaDB = AreaStorage.get();
    }

    public static AreaControl get()
    {
        if (instance == null)
        {
            instance = new AreaControl();
        }
        return instance;
    }
    
    public void loadDataBase()
    {
        Collection<AreaModel> models = areaDB.getAll();
        for (AreaModel model : models)
        {
            areas.put(model.getChunk(), new Area(model));
        }
    }
    
    public Group giveChunk(Chunk chunk, Group group)
    {
        if (!(group.equals(areas.get(chunk).getGroup())))
        {
            if (areas.get(chunk) == null)
            {
                group.addInfluence_used();
                
                AreaModel model = new AreaModel(chunk, group);
                areaDB.store(model); //TODO ID zuweisen ; selbes Problem bei Group
                return GroupControl.get().getWildLand();
            }
            else
            {
                group.addInfluence_used();
                areas.get(chunk).getGroup().remInfluence_used();
                
                AreaModel model = areas.get(chunk).model;
                model.setGroup(group);
                areaDB.update(model);
                return (areas.put(chunk, new Area(model))).getGroup();
            }
        }
        return null;//Chunk was already claimed
    }

    public Group getGroup(Location loc)
    {
        return getGroup(loc.getChunk());
    }

    public Group getGroup(Chunk chunk)
    {
        Group tmp = areas.get(chunk).getGroup();
        if (tmp == null)
        {
            return GroupControl.get().getWildLand();
        }
        return areas.get(chunk).getGroup();
    }
    
    public Group remChunk(Location loc)
    {
        return remChunk(loc.getChunk());
    }

    public Group remChunk(Chunk chunk)
    {
        AreaModel model = areas.get(chunk).model;
        Group group = areas.remove(chunk).getGroup();
        areaDB.delete(model);
        if (group != null)
        {
            group.remInfluence_used();
        }
        return group;
    }
    
    public void remAll(Group group)
    {
        List<Chunk> remlist = new ArrayList<Chunk>();
        for (Chunk chunk : areas.keySet())
        {
            if (areas.get(chunk).getGroup().equals(group))
            {
                remlist.add(chunk);
            }
        }
        for (Chunk chunk : remlist)
        {
            areaDB.delete(areas.get(chunk).model);
            areas.remove(chunk);
        }
        group.resetInfluence_used();
    }

    public void remAllAll()
    {
        areas.clear();
        areaDB.clear();
    }
    
    
}
