package de.cubeisland.cubeengine.basics.cheat;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class EnchantMatcher
{
    
    /**
     * Looks for matching enchantment
     * 
     * @param name the name to match
     * @return the matching Enchantment or null
     */
    public static Enchantment match(String name)
    {
        if (match(name,"arrowdamage","arrowdmg","arrdmg")) return Enchantment.ARROW_DAMAGE;
        if (match(name,"firearrow","farrow","arrowfire")) return Enchantment.ARROW_FIRE;
        if (match(name,"infarrow","infinitearrow","infinity")) return Enchantment.ARROW_INFINITE;
        if (match(name,"arrowkb","arrowknockback","kbarrow")) return Enchantment.ARROW_KNOCKBACK;
        if (match(name,"damage","alldamage","damageall","sharpness")) return Enchantment.DAMAGE_ALL;
        if (match(name,"ardamage","spiderdamage","spiderdmg","ardmg")) return Enchantment.DAMAGE_ARTHROPODS;
        if (match(name,"smite","undeaddmg","undeaddamage")) return Enchantment.DAMAGE_UNDEAD;
        if (match(name,"digspeed","effi","efficiency")) return Enchantment.DIG_SPEED;
        if (match(name,"dura","durability","unbreaking")) return Enchantment.DURABILITY;
        if (match(name,"fire","firedmg","firedamage","fireaspect")) return Enchantment.FIRE_ASPECT;
        if (match(name,"kb","knockback")) return Enchantment.KNOCKBACK;
        if (match(name,"fortune","lootbonusblocks","blockloot")) return Enchantment.LOOT_BONUS_BLOCKS;
        if (match(name,"looting","lootbonusmobs","mobloot")) return Enchantment.LOOT_BONUS_MOBS;
        if (match(name,"oxygen","longbreath","respire")) return Enchantment.OXYGEN;
        if (match(name,"protection","environementalprotection","envprot","prot")) return Enchantment.PROTECTION_ENVIRONMENTAL;
        if (match(name,"explosionprotection","expprot","blastprotection","blastprot")) return Enchantment.PROTECTION_EXPLOSIONS;
        if (match(name,"fallprotection","featherfalling","fallprot")) return Enchantment.PROTECTION_FALL;
        if (match(name,"fireprotection","fireprot")) return Enchantment.PROTECTION_FIRE;
        if (match(name,"projectileprotection","projprot","arrowprot")) return Enchantment.PROTECTION_PROJECTILE;
        if (match(name,"silktouch")) return Enchantment.SILK_TOUCH;
        if (match(name,"waterworker","aquaaffinity")) return Enchantment.WATER_WORKER;
        return null;
    }
    
    private static boolean match(String name, String... matchers)
    {
        for (String s : matchers)
        {
            if (s.equalsIgnoreCase(name))
                return true;
        }
        return false;
        
    }
    /**
     * Checks if the item can be enchanted safely
     * 
     * @param item the item to enchant
     * @param ench the Enchantment to apply
     * @return whether the enchantment is valid for this item or not
     */
    public static boolean matchItem(ItemStack item, Enchantment ench)
    {
        int enchs = item.getEnchantments().size();
        item.addEnchantment(ench, ench.getMaxLevel());
        if (enchs == item.getEnchantments().size())
            return false;
        item.removeEnchantment(ench);
        return true;
    }

}
