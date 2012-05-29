package de.cubeisland.cubeengine.basics.cheat;

import static de.cubeisland.cubeengine.CubeEngine._;
import de.cubeisland.cubeengine.basics.Perm;
import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.command.Command;
import de.cubeisland.cubeengine.core.command.CommandArgs;
import de.cubeisland.cubeengine.core.command.RequiresPermission;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
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
    UserManager cuManager = CubeCore.getInstance().getUserManager();
    Cheat cheat = new Cheat();
    //TODO constructor & register Cmds
    
    //@Flag({"unsafe","u"})
    //@Param(type=String.class)
    //@Param(type=Integer.class)
    @RequiresPermission
    //@Usage("<Enchantment> [level] [-unsafe]")
    //@Description("Adds an Enchantment to the item in your hand")
    @Command()//min=0,//max=2
    public void enchant(CommandSender sender, CommandArgs args)
    {
        //TODO language stuff
        //TODO command stuff
        User user = cuManager.getUser(sender);
        if (user == null)
        {
            sender.sendMessage(_("core","&cThis command can only be used ingame!"));
            return;
        }
        if (args.size()>0)
        {
            
            ItemStack item = user.getItemInHand();
            if (item.getType().equals(Material.AIR))
            {
                sender.sendMessage(_(user,"basics","&6ProTip: You cannot enchant your fists!"));
                //"&6Protipp: Du kannst deine Fäuste nicht verzaubern!"
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
                if (!Perm.COMMAND_ENCHANT_UNSAFE.isAuthorized(user))
                {
                    sender.sendMessage(_(user,"basics","&cYou are not allowed to use this command!"));
                    return;
                }
                cheat.unsafeEnchantItemInHand(user, ench, level);
                sender.sendMessage(_(user,"&aAdded unsafe Enchantment: &6%s&a to your item!",ench.toString()));
                //"&6Unsichere Verzauberung: &6%s&a zu deinem Item hinzugefügt!"
                return;
            }
            cheat.enchantItemInHand(user, ench, level);
            sender.sendMessage(_(user,"&aAdded Enchantment: &6%s&a to your item!",ench.toString()));
            //"&Verzauberung: &6%s&a zu deinem Item hinzugefügt!"
        }
        else
        {
            //description
            //usage (check permission if flag is allowed to use ?)

            sender.sendMessage("ARROW_DAMAGE: arrowdamage, arrowdmg, arrdmg");
            sender.sendMessage("ARROW_FIRE: firearrow, farrow, arrowfire");
            sender.sendMessage("ARROW_INFINITE: infarrow, infinitearrow, infinity");
            sender.sendMessage("ARROW_KNOCKBACK: arrowkb, arrowknockback, kbarrow");
            sender.sendMessage("DAMAGE_ALL: damage, alldamage, damageall, sharpness");
            sender.sendMessage("DAMAGE_ARTHROPODS: arthropodsdamage, spiderdamage, spiderdmg, ardmg");
            sender.sendMessage("DAMAGE_UNDEAD: smite, undeaddmg, undeaddamage");
            sender.sendMessage("DIG_SPEED: digspeed, effi, efficiency");
            sender.sendMessage("DURABILITY: dura, durability, unbreaking");
            sender.sendMessage("FIRE_ASPECT: fire, firedmg, firedamage, fireaspect");
            sender.sendMessage("KNOCKBACK: kb, knockback");
            sender.sendMessage("LOOT_BONUS_BLOCKS: fortune, lootbonusblocks, blockloot");
            sender.sendMessage("LOOT_BONUS_MOBS: looting, lootbonusmobs, mobloot");
            sender.sendMessage("OXYGEN: oxygen, longbreath, respire");
            sender.sendMessage("PROTECTION_ENVIRONMENTAL: protection, environementalprotection, envprot, prot");
            sender.sendMessage("PROTECTION_EXPLOSIONS: explosionprotection, expprot, blastprotection, blastprot");
            sender.sendMessage("PROTECTION_FALL: fallprotection, featherfalling, fallprot");
            sender.sendMessage("PROTECTION_FIRE: fireprotection, fireprot");
            sender.sendMessage("PROTECTION_PROJECTILE: projectileprotection, projprot, arrowprot");
            sender.sendMessage("SILK_TOUCH: silktouch");
            sender.sendMessage("WATER_WORKER: waterworker, aquaaffinity");
        }
    }
    
    //@Param(type=User.class)
    @RequiresPermission
    //@Usage("[Player]")
    //@Description("Refills your hunger bar")
    @Command()//min=0,//max=1
    public void feed(CommandSender sender, CommandArgs args)
    {
        User send = cuManager.getUser(sender);
        if (args.size()>0)
        {
            User user = args.getUser(1);
            if (user == null)
            {
                sender.sendMessage(_(send,"core","&cThe User %s does not exist!",args.getString(1)));
                return;
            }
            cheat.feed(user);
            sender.sendMessage(_(send,"basics","&6Feeded %s",user.getName()));
            user.sendMessage(_(user,"basics","&6You got fed by %s",sender.getName()));
        }
        else
        {
            cheat.feed(send);
            sender.sendMessage(_(send,"basics","&6You are now fed!"));
        }
    }
    
    //@Param(type=User.class)
    @RequiresPermission
    //@Usage("[Player]")
    //@Description("Changes the gamemode")
    @Command()//min=0,//max=1
    public void gamemode(CommandSender sender, CommandArgs args)
    {
        User send = cuManager.getUser(sender);
        if (args.size()>0)
        {
            User user = args.getUser(1);
            if (user == null)
            {
                sender.sendMessage(_(send,"core","&cThe User %s does not exist!",args.getString(1)));
                return;
            }
            cheat.gamemode(user, user.getGameMode()!=GameMode.CREATIVE);
            if (user.getGameMode() == GameMode.CREATIVE)
            {
                sender.sendMessage(_(send,"basics","&6%s is now in Creative-Mode!",user.getName()));
                user.sendMessage(_(user,"basics","&6Your gamemode was changed to: Creative"));
            }
            else
            {
                sender.sendMessage(_(send,"basics","&6%s is now in Survival-Mode!",user.getName()));
                user.sendMessage(_(user,"basics","&6Your gamemode was changed to: Survival"));
            }
        }
        else
        {
            cheat.gamemode(send, send.getGameMode()!=GameMode.CREATIVE);
            if (send.getGameMode() == GameMode.CREATIVE)
            {
                send.sendMessage(_(send,"basics","&6Your gamemode was changed to: Creative"));
            }
            else
            {
                send.sendMessage(_(send,"basics","&6Your gamemode was changed to: Survival"));
            }
        }
    }
    
    //@Param(type=User.class)
    //@Param(type=ItemStack.class)
    //@Param(type=Integer.class)
    //@Flag({"blacklist","b"})
    @RequiresPermission
    //@Usage("<Player> <Material[:Data]> [amount]")
    //@Description("Gives the specified Item to a player")
    @Command()//min=2,//max=3
    public void give(CommandSender sender, CommandArgs args)
    {
        //TODO blacklist items
    }
    
    //@Param(type=User.class)
    @RequiresPermission
    //@Usage("[Player]")
    //@Description("Heals a Player")
    @Command()//min=0,//max=1
    public void heal(CommandSender sender, CommandArgs args)
    {
        User send = cuManager.getUser(sender);
        if (args.size()>0)
        {
            User user = args.getUser(1);
            if (user == null)
            {
                sender.sendMessage(_(send,"core","&cThe User %s does not exist!",args.getString(1)));
                return;
            }
            cheat.heal(user);
            sender.sendMessage(_(send,"basics","&Healed %s",user.getName()));
            user.sendMessage(_(user,"basics","&6You got healed by %s",sender.getName()));
        }
        else
        {
            cheat.heal(send);
            sender.sendMessage(_(send,"basics","&6You are now healed!"));
        }
    }
    
    //@Param(type=User.class)
    //@Param(type=ItemStack.class)
    //@Param(type=Integer.class)
    //@Flag({"blacklist","b"})
    @RequiresPermission
    //@Usage("<Material[:Data]> [amount]")
    //@Description("Gives the specified Item to you")
    @Command()//min=1,//max=2
    public void item(CommandSender sender, CommandArgs args)
    {
        //TODO blacklist items -b Flag ignores Blacklist
    }
    
    @RequiresPermission
    //@Description("Refills the Stack in hand")
    @Command()//min=0,//max=0
    public void more(CommandSender sender, CommandArgs args)
    {
        User user = cuManager.getUser(sender);
        cheat.more(user);
        sender.sendMessage(_(user,"basics","Refilled Stack in Hand!"));
    }
    
    @RequiresPermission
    //@Param(type=String.class)
    //@Param(type=User.class)
    //@Usage("<day|night|dawn|even> [player] [-all]")
    //@Description("Changes the time for a player")
    @Command()//min=1,//max=2
    public void ptime(CommandSender sender, CommandArgs args)
    {
        long time = 0;
             if (args.getString(1).equalsIgnoreCase("day")) time = 12*1000;
        else if (args.getString(1).equalsIgnoreCase("night")) time = 0;
        else if (args.getString(1).equalsIgnoreCase("dawn")) time = 6*1000;
        else if (args.getString(1).equalsIgnoreCase("even")) time = 18*1000;
        User user;
        if (args.size()>1)
        {
            user = args.getUser(2);
        }
        else
        {
             user = cuManager.getUser(sender);
        }
        if (user == null)
        {
            sender.sendMessage(_(user,"core","&cThe User %s does not exist!",args.getString(1)));
            return;
        }
        cheat.ptime(user, time);
    }
    
    
    @RequiresPermission
    //@Flag({"all","a"})
    //@Usage("[-all]")
    //@Description("Repairs your items")
    @Command()//min=0,//max=0
    public void repair(CommandSender sender, CommandArgs args)
    {
        User user;
        if (sender instanceof Player)
        {
            user = cuManager.getUser(sender);
        }
        else
        {
            sender.sendMessage(_("core","&cThis command can only be used ingame!"));
            return;
        }
        if (args.hasFlag("a"))
        {
            List<ItemStack> list = cheat.repairAll(user);
            if (list.isEmpty())
            {
                sender.sendMessage(_(user,"basics","No items to repair!"));
            }
            else
            {
                String items = "";
                for (ItemStack item : list)
                {
                    items += " "+item.toString();
                }
                sender.sendMessage(_(user,"basics","Repaired %d items:%s!",list.size(),items));
            }
        }
        else
        {
            if (cheat.repairInHand(user))
                sender.sendMessage(_(user,"basics","Item repaired!"));
            else
                sender.sendMessage(_(user,"basics","Item cannot be repaired!"));
        }
    }
   
    @RequiresPermission
    //@Param(type=String.class)
    //@Param(type=World.class)
    //@Flag({"all","a"})
    //@Usage("<day|night|dawn|even> [world] [-all]")
    //@Description("Changes the time of a world")
    @Command()//min=1,//max=2
    public void time(CommandSender sender, CommandArgs args)
    {
        long time = 0;
             if (args.getString(1).equalsIgnoreCase("day")) time = 12*1000;
        else if (args.getString(1).equalsIgnoreCase("night")) time = 0;
        else if (args.getString(1).equalsIgnoreCase("dawn")) time = 6*1000;
        else if (args.getString(1).equalsIgnoreCase("even")) time = 18*1000;
        User user = cuManager.getUser(sender);
        World world = user.getWorld();
        if (args.size()>1)
        {
            world = sender.getServer().getWorld(args.getString(2));//TODO getWorld() in cmdargs
        }
        if (world == null)
        {
            sender.sendMessage(_(user,"core","&cThe World %s does not exist!",args.getString(2)));
            return;
        }
        if (args.hasFlag("all"))
        {
            for (World w : sender.getServer().getWorlds())
            {
                cheat.settime(w, time);
            }
            return;
        }
        cheat.settime(world, time);
    }
    
    public void unlimited(CommandSender sender, CommandArgs args)
    {
        
    }
    
}
