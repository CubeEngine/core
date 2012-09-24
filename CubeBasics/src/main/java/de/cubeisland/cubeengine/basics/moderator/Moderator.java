package de.cubeisland.cubeengine.basics.moderator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 *
 * @author Anselm Brehme
 */
public class Moderator //TODO remove or at least reduce this class
{
    public void broadcast(Player player, String message)
    {
        player.getServer().broadcastMessage(message);
    }
    
    public void clearinventory(Player player)
    {
        player.getInventory().clear();
        //?? save inventory to restore later ??
        player.updateInventory();
        player.sendMessage("Inventory cleared!");
        //TODO msg woanders
    }
    
    public int removeEntityType(Location loc, int radius, EntityType... types)
    {
        List<Entity> list = loc.getWorld().getEntities();
        Collection<EntityType> entitytypes = Arrays.asList(types);
        int removed = 0;
        for (Entity entity : list)
        {
            if (!entitytypes.contains(entity.getType()))
                continue;
            int distance = (int)(entity.getLocation().subtract(loc)).lengthSquared();
            if (radius != -1)
                if (radius*radius < distance)
                    continue;
            entity.remove();
            removed++;
        }
        return removed;
    }
    
    public int removeDrops(Location loc, int radius)
    {
        return removeEntityType(loc, radius, EntityType.DROPPED_ITEM);
    }
   
    public int removeArrows(Location loc, int radius)
    {
        return removeEntityType(loc, radius, EntityType.ARROW);
    }
    
    public int removeBoats(Location loc, int radius)
    {
        return removeEntityType(loc, radius, EntityType.BOAT);
    }
    
    public int removeMinecarts(Location loc, int radius)
    {
        return removeEntityType(loc, radius, EntityType.MINECART);
    }
    
    public int removeXp(Location loc, int radius)
    {
        return removeEntityType(loc, radius, EntityType.EXPERIENCE_ORB);
    }
    
    public int removePaintings(Location loc, int radius)
    {
        return removeEntityType(loc, radius, EntityType.PAINTING);
    }
    
    public void sudoCmd(Player player, String command)
    {
        player.chat("/"+command);
    }
    
    public void sudoMsg(Player player, String message)
    {
        player.chat(message);
    }
}
