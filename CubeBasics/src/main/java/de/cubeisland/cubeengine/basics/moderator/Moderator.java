package de.cubeisland.cubeengine.basics.moderator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;

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
    
    public void kill(Player player)
    {
        player.setHealth(0);
        player.getServer().broadcastMessage(player.getName() + " found his death!");
    }
    
    public void ping(Player player)
    {
        player.sendMessage("Pong!");
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
    
    public LivingEntity spawnMob(Location loc, EntityType type)
    {
        return loc.getWorld().spawnCreature(loc, type);
    }
    
    public List<LivingEntity> spawnMob(Location loc, EntityType type, int amount)
    {
        List<LivingEntity> list = new ArrayList<LivingEntity>();
        for (int i = 0 ; i < amount ; ++i)
        {
            list.add(spawnMob(loc,type));
        }
        return list;
    }
    
    public List<LivingEntity> spawnSheep(Location loc, DyeColor color, int amount)
    {
        List<LivingEntity> list = this.spawnMob(loc, EntityType.SHEEP, amount);
        for (LivingEntity sheep : list)
        {
            ((Sheep)sheep).setColor(color);
        }
        return list;
    }
    
    public List<LivingEntity> spawnCreeper(Location loc, boolean powered, int amount)
    {
        List<LivingEntity> list = this.spawnMob(loc, EntityType.CREEPER, amount);
        for (LivingEntity creeper : list)
        {
            ((Creeper)creeper).setPowered(powered);
        }
        return list;
    }
    
    public List<LivingEntity> spawnSlime(Location loc, int size, int amount)//TODO Size ??
    {
        List<LivingEntity> list = this.spawnMob(loc, EntityType.SLIME, amount);
        for (LivingEntity slime : list)
        {
            ((Slime)slime).setSize(size);
        }
        return list;
    }
    
    public List<LivingEntity> spawnMagmaCube(Location loc, int size, int amount)//TODO Size ??
    {
        List<LivingEntity> list = this.spawnMob(loc, EntityType.MAGMA_CUBE, amount);
        for (LivingEntity magmacube : list)
        {
            ((MagmaCube)magmacube).setSize(size);
        }
        return list;
    }
    
    public List<LivingEntity> spawnEnderman(Location loc, int itemId, int amount)
    {
        List<LivingEntity> list = this.spawnMob(loc, EntityType.ENDERMAN, amount);
        for (LivingEntity enderman : list)
        {
            ((Enderman)enderman).setCarriedMaterial((new ItemStack(itemId)).getData());
        }
        return list;
    }
    
    public List<LivingEntity> spawnWolf(Location loc, Player tamer, int amount)
    {
        List<LivingEntity> list = this.spawnMob(loc, EntityType.WOLF, amount);
        for (LivingEntity wolf : list)
        {
            if (tamer == null) break;
            ((Wolf)wolf).setOwner(tamer);
            ((Wolf)wolf).setTamed(true);
        }
        return list;
    }
    
    public List<LivingEntity> spawnOcelot(Location loc, Player tamer, int amount)
    {
        List<LivingEntity> list = this.spawnMob(loc, EntityType.OCELOT, amount);
        for (LivingEntity ocelot : list)
        {
            if (tamer == null) break;
            ((Ocelot)ocelot).setOwner(tamer);
            ((Ocelot)ocelot).setTamed(true);
        }
        return list;
    }
    
    public List<LivingEntity> spawnVillager(Location loc, Profession profession, int amount)
    {
        List<LivingEntity> list = this.spawnMob(loc, EntityType.VILLAGER, amount);
        for (LivingEntity villager : list)
        {
            ((Villager)villager).setProfession(profession);
        }
        return list;
    }
    
    
    public void sudoCmd(Player player, String command)
    {
        player.chat("/"+command);
    }
    
    public void sudoMsg(Player player, String message)
    {
        player.chat(message);
    }
    
    public void weather(World world, boolean sunny, boolean noThunder)
    {
        weather(world, sunny, noThunder, 10000000);
    }
    
    public void weather(World world, boolean sunny, boolean noThunder, int duration)
    {
        world.setStorm(sunny);
        world.setThundering(noThunder);
        world.setWeatherDuration(duration);
    }
    
    public void setspawn(World world, Location loc)
    {
        world.setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
