package de.cubeisland.cubeengine.basics.command.moderation.spawnmob;

import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class SpawningData
{
    public Location location;
    public EntityType entityType;
    public int amount = 1;
    private HashMap<EntityDataChanger,Object> data = new HashMap<EntityDataChanger, Object>();

    public void doSpawn()
    {
        if (entityType != null)
        {
            for (int i = 0; i < amount; i++)
            {
                Entity entity = this.location.getWorld().spawnEntity(this.location,this.entityType);
                for (Map.Entry<EntityDataChanger,Object> entry : data.entrySet())
                {
                    entry.getKey().applyTo(entity,entry.getValue());
                }
            }
        }
    }

    public <E,T> void add(EntityDataChanger<E,T> changer, T value)
    {
        this.data.put(changer,value);
    }

    public void showInfo(User user)
    {
    //TODO

    }
}
