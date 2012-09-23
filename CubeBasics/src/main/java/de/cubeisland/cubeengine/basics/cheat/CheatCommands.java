package de.cubeisland.cubeengine.basics.cheat;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import static de.cubeisland.cubeengine.core.CubeEngine._;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Anselm Brehme
 */
public class CheatCommands
{
    UserManager cuManager;

    public CheatCommands(Basics module)
    {
        cuManager = module.getUserManager();
    }

    //@Flag({"unsafe","u"})
    //@Param(type=String.class)
    //@Param(type=Integer.class)
    //@Usage("<Enchantment> [level] [-unsafe]")
    @Command(desc = "Adds an Enchantment to the item in your hand")//min=0,//max=2
    public void enchant(CommandContext context)
    {
        // TODO
        /*
         * User user = cuManager.getUser(sender);
         if (user == null)
         {
         user.sendMessage("&cThis command can only be used ingame!");
         return;
         }
         if (args.size() > 0)
         {

         ItemStack item = user.getItemInHand();
         if (item.getType().equals(Material.AIR))
         {
         user.sendMessage("&6ProTip: You cannot enchant your fists!");
         //"&6Protipp: Du kannst deine Fäuste nicht verzaubern!"
         return;
         }
         Enchantment ench = EnchantMatcher.match(args.getString(1));
         int level = ench.getMaxLevel();
         if (args.size() > 1)
         {
         level = args.getInt(2);
         }
         if (args.hasFlag("unsafe"))
         {
         if (!Perm.COMMAND_ENCHANT_UNSAFE.isAuthorized(user))
         {
         user.sendMessage("&cYou are not allowed to use this command!");
         return;
         }
         cheat.unsafeEnchantItemInHand(user, ench, level);
         user.sendMessage("&aAdded unsafe Enchantment: &6%s&a to your item!", ench.toString());
         //"&6Unsichere Verzauberung: &6%s&a zu deinem Item hinzugefügt!"
         return;
         }
         cheat.enchantItemInHand(user, ench, level);
         user.sendMessage("&aAdded Enchantment: &6%s&a to your item!", ench.toString());
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
         }*/
    }

    //@Param(type=User.class)
    //@Usage("[Player]")
    @Command(desc = "Refills your hunger bar")//min=0,//max=1
    public void feed(CommandContext context)
    {
        // TODO
        /*
         * User send = cuManager.getUser(sender);
         if (args.size() > 0)
         {
         User user = args.getUser(1);
         if (user == null)
         {
         send.sendTMessage("&cThe User %s does not exist!", args.getString(1));
         return;
         }
         cheat.feed(user);
         send.sendMessage("&6Feeded %s", user.getName());
         user.sendMessage("&6You got fed by %s", sender.getName());
         }
         else
         {
         cheat.feed(send);
         send.sendMessage("&6You are now fed!");
         }*/
    }

    //@Param(type=User.class)
    //@Usage("[Player]")
    //@Description("Changes the gamemode")
    @Command(
    names ={"gamemode", "gm"},
    max = 2,
    desc = "Changes the gamemode")
    public void gamemode(CommandContext context)
    {
        boolean changeOther = false;
        User user = cuManager.getUser(context.getSender());
        User sender = user;
        if (user == null)
        {
            context.getSender().sendMessage("You do not not have any gamemode!");
            return;
        }
        if (context.hasIndexed(2))
        {
            user = context.getIndexed(1, User.class, null);
            if (user == null)
            {
                // TODO invalid User msg
                return;
            }
            changeOther = true;
        }
        if (context.hasIndexed(0))
        {
            try
            {
                String mode = context.getIndexed(0, String.class);
                if (mode.equals("survival") || mode.equals("s"))
                {
                    user.setGameMode(GameMode.SURVIVAL);
                }
                else if (mode.equals("creative") || mode.equals("c"))
                {
                    user.setGameMode(GameMode.CREATIVE);
                }
                else if (mode.equals("adventure") || mode.equals("a"))
                {
                    user.setGameMode(GameMode.ADVENTURE);
                }
            }
            catch (ConversionException ex)
            {
                // TODO print usage
                return;
            }
        }
        else
        {
            GameMode gamemode = user.getGameMode();
            switch (gamemode)
            {
                case ADVENTURE:
                case CREATIVE:
                    user.setGameMode(GameMode.SURVIVAL);
                    break;
                case SURVIVAL:
                    user.setGameMode(GameMode.CREATIVE);
            }
        }
        if (changeOther)
        {
            sender.sendMessage("basics", "You changed the gamemode of %s to %s", user.getName(), _(sender, "basics", user.getGameMode().toString()));
            // TODO notify user who changed if flag set (permission)
            user.sendMessage("basics", "Your Gamemode has been changed to %s", _(user, "basics", user.getGameMode().toString()));
        }
        else
        {
            user.sendMessage("basics", "You changed sour gamemdoe to %s", _(user, "basics", user.getGameMode().toString()));
        }
    }

    //@Param(type=User.class)
    //@Param(type=ItemStack.class)
    //@Param(type=Integer.class)
    //@Usage("<Player> <Material[:Data]> [amount]")
    @Command(
    desc = "Gives the specified Item to a player",
    flags ={@Flag(name = "b", longName = "blacklist")},
    min = 2, max = 3)
    public void give(CommandContext context)
    {
        User sender = cuManager.getUser(context.getSender()); // TODO if sender is not a player
        User user = context.getIndexed(0, User.class, null);
        if (user == null)
        {
            // TODO no such player msg
            return;
        }
        ItemStack item = context.getIndexed(1, ItemStack.class, null);
        if (item == null)
        {
            // TODO no such item msg
            return;
        }

        if (context.hasFlag("b") && 1 == 0)//TODO ignore blacklist permission
        {
            if (1 == 0) // TODO Blacklist
            {
                sender.sendMessage("basics", "This item is blacklisted!");
                return;
            }
        }

        int amount = item.getMaxStackSize();
        if (context.hasIndexed(3))
        {
            amount = context.getIndexed(2, int.class, 0);
            if (amount == 0)
            {
                // TODO invalid amount
                return;
            }
        }
        item.setAmount(amount);


        user.getInventory().addItem(item);
        user.updateInventory();
        sender.sendMessage("basics", "You gave %s %d %s", user.getName(), item.toString(), amount);
        // TODO other message so user do not know who gave the items
        // Flag for no message when giving items
        user.sendMessage("%s just gave you %d %s", sender.getName(), item.toString(), amount);
    }

    //@Param(type=User.class)
    //@Usage("[Player]")
    @Command(desc = "Heals a Player", max = 1)//min=0,//max=1
    public void heal(CommandContext context)
    {
        // TODO
        /*User send = cuManager.getUser(sender);
         if (args.size() > 0)
         {
         User user = args.getUser(1);
         if (user == null)
         {
         send.sendTMessage("&cThe User %s does not exist!", args.getString(1));
         return;
         }
         cheat.heal(user);
         send.sendMessage("&Healed %s", user.getName());
         user.sendMessage("&6You got healed by %s", sender.getName());
         }
         else
         {
         cheat.heal(send);
         send.sendMessage("&6You are now healed!");
         }*/
    }

    //@Param(type=User.class)
    //@Param(type=ItemStack.class)
    //@Param(type=Integer.class)
    //@Usage("<Material[:Data]> [amount]")
    @Command(
    names ={"item", "i"},
    desc = "Gives the specified Item to you",
    max = 2,
    min = 1,
    flags ={@Flag(longName = "blacklist", name = "b")})
    public void item(CommandContext context)
    {
        User sender = context.getIndexed(0, User.class, null);
        if (sender == null)
        {
            // TODO msg no player
            return;
        }
        ItemStack item = context.getIndexed(0, ItemStack.class, null);
        if (item == null)
        {
            // TODO msg invalid item
            return;
        }

        if (context.hasFlag("b") && 1 == 0)//TODO ignore blacklist permission
        {
            if (1 == 0) // TODO Blacklist
            {
                sender.sendMessage("basics", "This item is blacklisted!");
                return;
            }
        }

        int amount = item.getMaxStackSize();
        if (context.hasIndexed(2))
        {
            amount = context.getIndexed(1, int.class, 0);
            if (amount == 0)
            {
                // TODO msg invalid amount
                return;
            }
        }
        item.setAmount(amount);
        sender.getInventory().addItem(item);
        sender.updateInventory();
        sender.sendMessage("Received: %d %s ", item.toString(), amount); // TODO item.toString is no good
    }

    @Command(desc = "Refills the Stack in hand")//min=0,//max=0
    public void more(CommandContext context)
    {
        User user = cuManager.getUser(context.getSender());
        if (user == null)
        {
            // TODO msg no player
            return;
        }
        user.getItemInHand().setAmount(64);
        user.sendMessage("basics", "Refilled Stack in Hand!");
    }

    //@Param(type=String.class)
    //@Param(type=User.class)
    //@Usage("<day|night|dawn|even> [player] [-all]")
    @Command(
    desc = "Changes the time for a player",
    min = 1,
    max = 2,
    flags ={@Flag(longName = "all", name = "a")})
    public void ptime(CommandContext context)
    {
        //TODO
        /*long time = 0;
         if (args.getString(1).equalsIgnoreCase("day"))
         {
         time = 12 * 1000;
         }
         else if (args.getString(1).equalsIgnoreCase("night"))
         {
         time = 0;
         }
         else if (args.getString(1).equalsIgnoreCase("dawn"))
         {
         time = 6 * 1000;
         }
         else if (args.getString(1).equalsIgnoreCase("even"))
         {
         time = 18 * 1000;
         }
         User user;
         if (args.size() > 1)
         {
         user = args.getUser(2);
         }
         else
         {
         user = cuManager.getUser(sender);
         }
         if (user == null)
         {
         user.sendTMessage("&cThe User %s does not exist!", args.getString(1));
         return;
         }
         cheat.ptime(user, time);*/
    }

    //@Flag({"all","a"})
    //@Usage("[-all]")
    //@Description("Repairs your items")
    @Command(
    desc = "Repairs your items", 
    flags ={@Flag(longName = "all", name = "a")})//min=0,//max=0
    public void repair(CommandContext context)
    {
        //TODO
        /*
         * User user;
         if (sender instanceof Player)
         {
         user = cuManager.getUser(sender);
         }
         else
         {
         sender.sendMessage(_("core", "&cThis command can only be used ingame!"));
         return;
         }
         if (args.hasFlag("a"))
         {
         List<ItemStack> list = cheat.repairAll(user);
         if (list.isEmpty())
         {
         user.sendTMessage("No items to repair!");
         }
         else
         {
         String items = "";
         for (ItemStack item : list)
         {
         items += " " + item.toString();
         }
         user.sendTMessage("Repaired %d items:%s!", list.size(), items);
         }
         }
         else
         {
         if (cheat.repairInHand(user))
         {
         user.sendTMessage("Item repaired!");
         }
         else
         {
         user.sendTMessage("Item cannot be repaired!");
         }
         }*/
    }

    //@Param(type=String.class)
    //@Param(type=World.class)
    //@Flag({"all","a"})
    //@Usage("<day|night|dawn|even> [world] [-all]")
    //@Description("Changes the time of a world")
    @Command(desc = "Changes the time of a world")//min=1,//max=2
    public void time(CommandContext context)
    {
        //TODO
        /*long time = 0;
         if (args.getString(1).equalsIgnoreCase("day"))
         {
         time = 12 * 1000;
         }
         else if (args.getString(1).equalsIgnoreCase("night"))
         {
         time = 0;
         }
         else if (args.getString(1).equalsIgnoreCase("dawn"))
         {
         time = 6 * 1000;
         }
         else if (args.getString(1).equalsIgnoreCase("even"))
         {
         time = 18 * 1000;
         }
         User user = cuManager.getUser(sender);
         World world = user.getWorld();
         if (args.size() > 1)
         {
         world = sender.getServer().getWorld(args.getString(2));//TODO getWorld() in cmdargs
         }
         if (world == null)
         {
         user.sendTMessage("&cThe World %s does not exist!", args.getString(2));
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
         cheat.settime(world, time);*/
    }

    public void unlimited(CommandContext context)
    {
        //TODO
    }
}
