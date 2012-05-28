package de.cubeisland.cubeengine.basics.cheat;

import de.cubeisland.cubeengine.basics.Perm;
import de.cubeisland.cubeengine.core.command.Command;
import de.cubeisland.cubeengine.core.command.CommandArgs;
import de.cubeisland.cubeengine.core.command.RequiresPermission;
import static de.cubeisland.cubeengine.CubeEngine._;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class CheatCommands
{
 
    //@Flag({"unsafe","u"})
    //@Param(type=String.class)
    //@Param(type=Integer.class)
    @RequiresPermission
    //@Usage("<Enchantment> [level]")
    //@Description("Adds an Enchantment to the item in your hand")
    @Command()//min=0,//max=2
    public void enchant(CommandSender sender, CommandArgs args)
    {
        if (!(sender instanceof Player)) return;
        if (args.size()>0)
        {
            Player player = (Player)sender;
            ItemStack item = player.getItemInHand();
            if (item.getType().equals(Material.AIR))
            {
                sender.sendMessage("&6ProTip: You cannot enchant your fists!");
                //TODO "&6Protipp: Du kannst deine Fäuste nicht verzaubern!"
                return;
            }
            Enchantment ench = EnchantMatcher.match(args.getString(1));
            int level = ench.getMaxLevel();
            if (args.size()>1)
            {
                level = args.getInt(2);
            }
            if (args.hasFlag("unsafe"))
            {
                //TODO check Permissions 
                if (!Perm.COMMAND_ENCHANT_UNSAFE.isAuthorized(player))
                {
                    //TODO not allowed msg
                    return;
                }
                Cheat.unsafeEnchantItemInHand(player, ench, level);
                sender.sendMessage(_("&aAdded unsafe Enchantment: &6%s&a to your item!",ench.toString()));
                //"&6Unsichere Verzauberung: &6%s&a zu deinem Item hinzugefügt!"
                return;
            }
            Cheat.enchantItemInHand(player, ench, level);
            sender.sendMessage(_("&aAdded Enchantment: &6%s&a to your item!",ench.toString()));
            //"&Verzauberung: &6%s&a zu deinem Item hinzugefügt!"
            //TODO language
        }
        else
        {
            //TODO show availiable enchs
            /*
        "arrowdamage","arrowdmg","arrdmg"   |   ARROW_DAMAGE;
        "firearrow","farrow","arrowfire"   |   ARROW_FIRE;
        "infarrow","infinitearrow","infinity"   |   ARROW_INFINITE;
        "arrowkb","arrowknockback","kbarrow"   |   ARROW_KNOCKBACK;
        "damage","alldamage","damageall","sharpness"   |   DAMAGE_ALL;
        "ardamage","spiderdamage","spiderdmg","ardmg"   |   DAMAGE_ARTHROPODS;
        "smite","undeaddmg","undeaddamage"   |   DAMAGE_UNDEAD;
        "digspeed","effi","efficiency"   |   DIG_SPEED;
        "dura","durability","unbreaking"   |   DURABILITY;
        "fire","firedmg","firedamage","fireaspect"   |   FIRE_ASPECT;
        "kb","knockback"   |   KNOCKBACK;
        "fortune","lootbonusblocks","blockloot"   |   LOOT_BONUS_BLOCKS;
        "looting","lootbonusmobs","mobloot"   |   LOOT_BONUS_MOBS;
        "oxygen","longbreath","respire"   |   OXYGEN;
        "protection","environementalprotection","envprot","prot"   |   PROTECTION_ENVIRONMENTAL;
        "explosionprotection","expprot","blastprotection","blastprot"   |   PROTECTION_EXPLOSIONS;
        "fallprotection","featherfalling","fallprot"   |   PROTECTION_FALL;
        "fireprotection","fireprot"   |   PROTECTION_FIRE;
        "projectileprotection","projprot","arrowprot"   |   PROTECTION_PROJECTILE;
        "silktouch"   |   SILK_TOUCH;
        "waterworker","aquaaffinity"   |   WATER_WORKER;
             */
        }
    }
}
