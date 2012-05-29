package de.cubeisland.cubeengine.basics.cheat;

import java.util.ArrayList;
import java.util.List;
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
    public void gamemode(Player player, boolean bln)
    {
        if (bln)
            player.setGameMode(GameMode.CREATIVE);
        else
            player.setGameMode(GameMode.SURVIVAL);
    }
    
    public void item(Player player, ItemStack item)
    {
        player.getInventory().addItem(item);
    }
    
    public void more(Player player)
    {
        player.getItemInHand().setAmount(player.getItemInHand().getMaxStackSize());
    }
    
    public void feed(Player player, int amount)
    {
        player.setFoodLevel(Math.max(20, player.getFoodLevel()+amount));
    }
    
    public void feed(Player player)
    {
        player.setFoodLevel(20);
    }
    
    public void enchantItemInHand(Player player, Enchantment ench, int level)
    {
        player.getItemInHand().addEnchantment(ench, level);
    }
    
    public void unsafeEnchantItemInHand(Player player, Enchantment ench, int level)
    {
        player.getItemInHand().addUnsafeEnchantment(ench, level);
    }
    
    public void heal(Player player, int amount)
    {
        player.setHealth(Math.max(20, player.getHealth()+amount));
    }
    
    public void heal(Player player)
    {
        player.setHealth(20);
    }
    
    public void settime(World world, long time)
    {
        world.setTime(time);
    }
    
    public boolean repairInHand(Player player)
    {
        //TODO check if it is repairable
        player.getItemInHand().setDurability((short)0);
        return true;
    }
    
    public List<ItemStack> repairAll(Player player)
    {
        List<ItemStack> list = new ArrayList<ItemStack>();
        //TODO repair all its inventory
        //return List of repaired items
        return list;
    }
    
    public void ptime(Player player, long time)
    {
        player.setPlayerTime(time, false);
    }
    
    public void resetptime(Player player)
    {
        player.resetPlayerTime();
    }
}
