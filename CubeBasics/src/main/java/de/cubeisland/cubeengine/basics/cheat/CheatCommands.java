package de.cubeisland.cubeengine.basics.cheat;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Anselm Brehme
 */
public class CheatCommands
{
    UserManager cuManager = CubeEngine.getUserManager();
    Cheat cheat = new Cheat();

    //@Flag({"unsafe","u"})
    //@Param(type=String.class)
    //@Param(type=Integer.class)
    //@Usage("<Enchantment> [level] [-unsafe]")
    @Command(desc = "Adds an Enchantment to the item in your hand")//min=0,//max=2
    public void enchant(CommandSender sender)
    {
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
    public void feed(CommandSender sender)
    {
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
    @Command(desc = "Changes the gamemode")//min=0,//max=1
    public void gamemode(CommandSender sender)
    {
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
            cheat.gamemode(user, user.getGameMode() != GameMode.CREATIVE);
            if (user.getGameMode() == GameMode.CREATIVE)
            {
                send.sendMessage("&6%s is now in Creative-Mode!", user.getName());
                user.sendMessage("&6Your gamemode was changed to: Creative");
            }
            else
            {
                send.sendMessage("&6%s is now in Survival-Mode!", user.getName());
                user.sendMessage("&6Your gamemode was changed to: Survival");
            }
        }
        else
        {
            cheat.gamemode(send, send.getGameMode() != GameMode.CREATIVE);
            if (send.getGameMode() == GameMode.CREATIVE)
            {
                send.sendMessage("&6Your gamemode was changed to: Creative");
            }
            else
            {
                send.sendMessage("&6Your gamemode was changed to: Survival");
            }
        }*/
    }

    //@Param(type=User.class)
    //@Param(type=ItemStack.class)
    //@Param(type=Integer.class)
    //@Usage("<Player> <Material[:Data]> [amount]")
    @Command(
    desc = "Gives the specified Item to a player",
    flags =
    {
        @Flag(name = "b", longName = "blacklist")
    }, min = 2, max = 3)
    public void give(CommandSender sender)
    {
        /*
         * User user = args.getUser(1);
        User send = cuManager.getUser(sender);
        ItemStack item = args.getItemStack(2);
        int amount = item.getMaxStackSize();
        if (args.size() > 2)
        {
            amount = args.getInt(3);
        }
        item.setAmount(amount);
        if (1 == 1)//TODO item is blacklisted
        {
            if (args.hasFlag("blacklist"))
            {
                if (Perm.COMMAND_GIVE_BLACKLIST.isAuthorized(user))
                {
                    send.sendMessage("You gave %s %s x&d", user.getName(), item.toString(), amount);
                    user.sendMessage("%s just gave you %s x&d", send.getName(), item.toString(), amount);
                    cheat.item(user, item);
                    return;
                }
            }
            send.sendMessage("This item is blacklisted!");
            return;
        }
        cheat.item(user, item);
        send.sendMessage("You gave %s %s x&d", user.getName(), item.toString(), amount);
        user.sendMessage("%s just gave you %s x&d", send.getName(), item.toString(), amount);
        */
    }

    //@Param(type=User.class)
    //@Usage("[Player]")
    @Command(desc = "Heals a Player", max = 1)//min=0,//max=1
    public void heal(CommandSender sender)
    {
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
    desc = "Gives the specified Item to you",
    max = 2,
    min = 1,
    flags =
    {
        @Flag(longName = "blacklist", name = "b")
    })
    public void item(CommandSender sender)
    {
       /* User user = cuManager.getUser(sender);
        ItemStack item = args.getItemStack(2);
        int amount = item.getMaxStackSize();
        if (args.size() > 2)
        {
            amount = args.getInt(3);
        }
        item.setAmount(amount);
        if (1 == 1)//TODO item is blacklisted
        {
            if (args.hasFlag("blacklist"))
            {
                if (Perm.COMMAND_ITEM_BLACKLIST.isAuthorized(user))
                {
                    user.sendMessage("Received: %s x&d", item.toString(), amount);
                    cheat.item(user, item);
                    return;
                }
            }
            user.sendMessage("This item is blacklisted!");
            return;
        }
        cheat.item(user, item);
        user.sendMessage("Received: %s x&d", item.toString(), amount);*/
    }

    @Command(desc = "Refills the Stack in hand")//min=0,//max=0
    public void more(CommandSender sender)
    {
        User user = cuManager.getUser(sender);
        cheat.more(user);
        user.sendMessage("Refilled Stack in Hand!");
    }

    //@Param(type=String.class)
    //@Param(type=User.class)
    //@Usage("<day|night|dawn|even> [player] [-all]")
    @Command(
            desc = "Changes the time for a player",
    min = 1,
    max = 2,
    flags =
    {
        @Flag(longName = "all", name = "a")
    })
    public void ptime(CommandSender sender)
    {
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
    @Command(desc="Repairs your items",flags={
        @Flag(longName="all",name="a")
    })//min=0,//max=0
    public void repair(CommandSender sender)
    {
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
    @Command(desc="Changes the time of a world")//min=1,//max=2
    public void time(CommandSender sender)
    {
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

    public void unlimited(CommandSender sender)
    {
    }
}
