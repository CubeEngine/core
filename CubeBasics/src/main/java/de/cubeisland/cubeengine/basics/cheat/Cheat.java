package de.cubeisland.cubeengine.basics.cheat;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Anselm Brehme
 */
public class Cheat  //TODO remove or at least reduce this class
{
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
    
    public void ptime(Player player, long time)
    {
        player.setPlayerTime(time, false);
    }
    
    public void resetptime(Player player)
    {
        player.resetPlayerTime();
    }
}