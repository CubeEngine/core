package de.cubeisland.cubeengine.basics.cheat;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class Cheat
{
    /**
     * Sets the GameMode of Player
     * 
     * @param player the player to set its gamemode
     * @param bln true=CREATIVE false=SURVIVAL
     */
    public static void gamemode(Player player, boolean bln)
    {
        if (bln)
            player.setGameMode(GameMode.CREATIVE);
        else
            player.setGameMode(GameMode.SURVIVAL);
    }
    
    public static void item(Player player, ItemStack item)
    {
        player.getInventory().addItem(item);
    }
    
    public static void more(Player player)
    {
        player.getItemInHand().setAmount(player.getItemInHand().getMaxStackSize());
    }
    
    public static void feed(Player player, int amount)
    {
        player.setFoodLevel(Math.max(20, player.getFoodLevel()+amount));
    }
    
    public static void feed(Player player)
    {
        player.setFoodLevel(20);
    }
    
    public static void enchantItemInHand(Player player, Enchantment ench, int level)
    {
        player.getItemInHand().addEnchantment(ench, level);
    }
    
    public static void unsafeEnchantItemInHand(Player player, Enchantment ench, int level)
    {
        player.getItemInHand().addUnsafeEnchantment(ench, level);
    }
    
    public static void heal(Player player, int amount)
    {
        player.setHealth(Math.max(20, player.getHealth()+amount));
    }
    
    public static void heal(Player player)
    {
        player.setHealth(20);
    }
    
    public static void settime(World world, long time)
    {
        world.setTime(time);
    }
    
    public static boolean repairInHand(Player player)
    {
        //TODO check if it is repairable
        player.getItemInHand().setDurability((short)0);
        return true;
    }
    
    public static boolean repairAll(Player player)
    {
        //TODO repair all its inventory
        //return List of repaired items
        return true;
    }
    
    public static void ptime(Player player, long time)
    {
        player.setPlayerTime(time, false);
    }
    
    public static void resetptime(Player player)
    {
        player.resetPlayerTime();
    }
}
