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
 * @author Faithcaio
 */
public class Moderator
{
    public static void broadcast(Player player, String message)
    {
        player.getServer().broadcastMessage(message);
    }
    
    public static void clearinventory(Player player)
    {
        player.getInventory().clear();
        //?? save inventory to restore later ??
        player.updateInventory();
        player.sendMessage("Inventory cleared!");
        //TOdO msg
    }
    
    public static void kill(Player player)
    {
        player.setHealth(0);
        player.getServer().broadcastMessage(player.getName() + " found his death!");
    }
    
    public static void ping(Player player)
    {
        player.sendMessage("Pong!");
    }
    
    public static int removeEntityType(Location loc, int radius, EntityType... types)
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
    
    public static int removeDrops(Location loc, int radius)
    {
        return removeEntityType(loc, radius, EntityType.DROPPED_ITEM);
    }
   
    public static int removeArrows(Location loc, int radius)
    {
        return removeEntityType(loc, radius, EntityType.ARROW);
    }
    
    public static int removeBoats(Location loc, int radius)
    {
        return removeEntityType(loc, radius, EntityType.BOAT);
    }
    
    public static int removeMinecarts(Location loc, int radius)
    {
        return removeEntityType(loc, radius, EntityType.MINECART);
    }
    
    public static int removeXp(Location loc, int radius)
    {
        return removeEntityType(loc, radius, EntityType.EXPERIENCE_ORB);
    }
    public static int removePaintings(Location loc, int radius)
    {
        return removeEntityType(loc, radius, EntityType.PAINTING);
    }
    
    public static void sudoCmd(Player player, String command)
    {
        player.chat("/"+command);
    }
    
    public static void sudoMsg(Player player, String message)
    {
        player.chat(message);
    }
            
}
