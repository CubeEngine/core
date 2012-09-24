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
    
    public boolean repairInHand(Player player)
    {
        ItemStack item = player.getItemInHand();
        if (!this.checkRepairableItem(item)) return false;
        player.getItemInHand().setDurability((short)0);
        return true;
    }
    
    private boolean checkRepairableItem(ItemStack item)
    {
        switch (item.getType())
        {
            case IRON_SPADE: case IRON_PICKAXE: case IRON_AXE: case IRON_SWORD:
            case WOOD_SPADE: case WOOD_PICKAXE: case WOOD_AXE: case WOOD_SWORD:
            case STONE_SPADE: case STONE_PICKAXE: case STONE_AXE: case STONE_SWORD:
            case DIAMOND_SPADE: case DIAMOND_PICKAXE: case DIAMOND_AXE: case DIAMOND_SWORD:
            case GOLD_SPADE: case GOLD_PICKAXE: case GOLD_AXE: case GOLD_SWORD:
            case WOOD_HOE: case STONE_HOE: case IRON_HOE: case DIAMOND_HOE: case GOLD_HOE:
            case LEATHER_HELMET: case LEATHER_CHESTPLATE: case LEATHER_LEGGINGS: case LEATHER_BOOTS:    
            case CHAINMAIL_HELMET: case CHAINMAIL_CHESTPLATE: case CHAINMAIL_LEGGINGS: case CHAINMAIL_BOOTS:   
            case IRON_HELMET: case IRON_CHESTPLATE: case IRON_LEGGINGS: case IRON_BOOTS:   
            case DIAMOND_HELMET: case DIAMOND_CHESTPLATE: case DIAMOND_LEGGINGS: case DIAMOND_BOOTS:   
            case GOLD_HELMET: case GOLD_CHESTPLATE: case GOLD_LEGGINGS: case GOLD_BOOTS:
            case FLINT_AND_STEEL: case BOW: case FISHING_ROD: case SHEARS: return true;
            default: return false;
        }
    }
    
    public List<ItemStack> repairAll(Player player)
    {
        List<ItemStack> list = new ArrayList<ItemStack>();
        for (ItemStack item : player.getInventory().getContents())
        {
            if (this.checkRepairableItem(item))
            {
                list.add(item);
                item.setDurability((short)0);
            }
        }
        for (ItemStack item : player.getInventory().getArmorContents())
        {
            if (this.checkRepairableItem(item))
            {
                list.add(item);
                item.setDurability((short)0);
            }
        }
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
