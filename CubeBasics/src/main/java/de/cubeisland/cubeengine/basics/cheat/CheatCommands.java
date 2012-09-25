package de.cubeisland.cubeengine.basics.cheat;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.Perm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.MaterialMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
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

    @Command(
    desc = "Adds an Enchantment to the item in your hand",
    min = 1,
    max = 2,
    flags = {@Flag(longName = "unsafe", name = "u")},
    usage = "/enchant <enchantment> [level] [-unsafe]")
    public void enchant(CommandContext context)
    {
        User sender = cuManager.getUser(context.getSender());
        if (sender == null)
        {
            invalidUsage(context.getSender(), "core", "&cThis command can only be used by a player!");
        }
        ItemStack item = sender.getItemInHand();
        if (item.getType().equals(Material.AIR))
        {
            invalidUsage(context.getSender(), "basics", "&6ProTip: You cannot enchant your fists!");
        }
        Enchantment ench = null;
        if (context.hasIndexed(1))
        {
            ench = context.getIndexed(0, Enchantment.class, null);
        }
        if (ench == null)
        {
            invalidUsage(context.getSender(), "basics", "Enchantment not found! Try one of those instead:");
            // TODO list possible enchantments for item in hand
            return;
        }
        int level = ench.getMaxLevel();
        if (context.hasIndexed(2))
        {
            level = context.getIndexed(1, int.class, 0);
            if (level <= 0)
            {
                invalidUsage(context.getSender(), "basics", "The EnchantmentLevel has to be a Number greater than 0!");
            }
        }
        if (context.hasFlag("u")) // Add unsafe enchantment //TODO permission
        {
            if (Perm.COMMAND_ENCHANT_UNSAFE.isAuthorized(sender))
            {
                item.addUnsafeEnchantment(ench, level);
                sender.sendMessage("bascics", "&aAdded unsafe Enchantment: &6%s&a to your item!", ench.toString()); //TODO use other than ench.toString AND add level
                return;
            }
            denyAccess(sender, "basics", "You are not allowed to add unsafe enchantments!");
        }
        else
        {
            if (ench.canEnchantItem(item))
            {
                if ((level < ench.getStartLevel()) || (level > ench.getMaxLevel()))
                {
                    item.addUnsafeEnchantment(ench, level);
                    sender.sendMessage("bascics", "&aAdded Enchantment: &6%s&a to your item!", ench.toString());//TODO use other than ench.toString  AND add level
                    return;
                }
                invalidUsage(context.getSender(), "basics", "This enchantmentlevel is not allowed!");
            }
            invalidUsage(context.getSender(), "basics", "This enchantment is not allowed for this item!");
        }
    }

    @Command(
    desc = "Refills your hunger bar",
    max = 1,
    usage = "/feed [player]")
    public void feed(CommandContext context)
    {
        User sender = cuManager.getUser(context.getSender());
        if (sender == null)
        {
            invalidUsage(context.getSender(), "basics", "&cDon't feed the troll!");
        }
        User user = sender;
        boolean other = false;
        if (context.hasIndexed(0))
        {
            user = context.getIndexed(0, User.class, null);
            if (user == null)
            {
                invalidUsage(context.getSender(), "core", "&cThe User %s does not exist!", context.getString(0));
                //TODO invalidArgumentException or smth like that  with invalidUser() <- I do need this VERY often
            }
            other = true;
        }
        user.setFoodLevel(20);
        user.setSaturation(20);
        user.setExhaustion(0);
        if (other)
        {
            sender.sendMessage("basics", "&6Feeded %s", user.getName());
            user.sendMessage("basics", "&6You got fed by %s", sender.getName());
        }
        else
        {
            sender.sendMessage("basics", "&6You are now fed!");
        }
    }

    @Command(
    names = {"gamemode", "gm"},
    max = 2,
    desc = "Changes the gamemode",
    usage = "/gm <gamemode> [player]")
    public void gamemode(CommandContext context)
    {
        boolean changeOther = false;
        User user = cuManager.getUser(context.getSender());
        User sender = user;
        if (user == null)
        {
            invalidUsage(context.getSender(), "basics", "&cYou do not not have any gamemode!");
        }
        if (context.hasIndexed(1))
        {
            user = context.getIndexed(1, User.class, null);
            if (user == null)
            {
                invalidUsage(context.getSender(), "core", "&cThe User %s does not exist!", context.getString(0));
            }
            changeOther = true;
        }
        if (!Perm.COMMAND_GAMEMODE_OTHER.isAuthorized(sender))
        {
            denyAccess(context.getSender(), "basics", "You do not have permission to change the gamemode of an other player!");
        }
        if (context.hasIndexed(0))
        {
            String mode = context.getString(0);
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
            // TODO later notify user who changed if flag set (permission)
            user.sendMessage("basics", "Your Gamemode has been changed to %s", _(user, "basics", user.getGameMode().toString()));
        }
        else
        {
            user.sendMessage("basics", "You changed your gamemode to %s", _(user, "basics", user.getGameMode().toString()));
        }
    }

    @Command(
    desc = "Heals a Player",
    max = 1,
    usage = "/heal [player]")
    public void heal(CommandContext context)
    {
        User sender = cuManager.getUser(context.getSender());
        if (sender == null)
        {
            invalidUsage(context.getSender(), "basics", "&cOnly time can heal your wounds!");
        }
        User user = sender;
        boolean other = false;
        if (context.hasIndexed(0))
        {
            user = context.getUser(0);
            if (user == null)
            {
                invalidUsage(context.getSender(), "core", "&cThe User %s does not exist!", context.getString(0));
                return;
            }
            other = true;
        }
        user.setFoodLevel(20);
        user.setSaturation(20);
        user.setExhaustion(0);
        if (other)
        {
            sender.sendMessage("basics", "&6Healed %s", user.getName());
            user.sendMessage("basics", "&6You got healed by %s", sender.getName());
        }
        else
        {
            sender.sendMessage("basics", "&6You are now healed!");
        }
    }

    @Command(
    desc = "Gives the specified Item to a player",
    flags = {@Flag(name = "b", longName = "blacklist")},
    min = 2, max = 3,
    usage = "/give <player> <material[:data]> [amount] [-blacklist]")
    public void give(CommandContext context)
    {
        User sender = cuManager.getUser(context.getSender());
        User user = context.getIndexed(0, User.class, null);
        if (user == null)
        {
            invalidUsage(context.getSender(), "core", "&cThe User %s does not exist!", context.getString(0));
        }
        ItemStack item = context.getIndexed(1, ItemStack.class, null);
        if (item == null)
        {
            invalidUsage(context.getSender(), "core", "&cUnknown Item: %s!", context.getString(1));
            //TODO invalidArgumentException or smth like that  with invalidItem() <- I do need this quite often
        }
        if (context.hasFlag("b") && Perm.COMMAND_GIVE_BLACKLIST.isAuthorized(sender))
        {
            if (1 == 0) // TODO Blacklist
            {
                denyAccess(context.getSender(), "basics", "&cThis item is blacklisted!");
            }
        }
        int amount = item.getMaxStackSize();
        if (context.hasIndexed(3))
        {
            amount = context.getIndexed(2, int.class, 0);
            if (amount == 0)
            {
                invalidUsage(context.getSender(), "basics", "&cThe amount has to be a number greater than 0!");
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
    
    @Command(
    names = {"item", "i"},
    desc = "Gives the specified Item to you",
    max = 2,
    min = 1,
    flags = {@Flag(longName = "blacklist", name = "b")},
    usage = "/i <material[:data]> [amount] [-blacklist]")
    public void item(CommandContext context)
    {
        User sender = cuManager.getUser(context.getSender());
        if (sender == null)
        {
            invalidUsage(context.getSender(), "core", "&cThis command can only be used by a player!");
        }
        ItemStack item = context.getIndexed(0, ItemStack.class, null);
        if (item == null)
        {
            invalidUsage(context.getSender(), "core", "&cUnknown Item: %s!", context.getString(1));
        }

        if (context.hasFlag("b") && Perm.COMMAND_ITEM_BLACKLIST.isAuthorized(sender))
        {
            if (1 == 0) // TODO Blacklist
            {
                denyAccess(context.getSender(), "basics", "&cThis item is blacklisted!");
            }
        }

        int amount = item.getMaxStackSize();
        if (context.hasIndexed(2))
        {
            amount = context.getIndexed(1, int.class, 0);
            if (amount == 0)
            {
                invalidUsage(context.getSender(), "basics", "The amount has to be a Number greater than 0!");
            }
        }
        item.setAmount(amount);
        sender.getInventory().addItem(item);
        sender.updateInventory();
        sender.sendMessage("Received: %d %s ", item.toString(), amount); // TODO item.toString is no good
    }

    @Command(
    desc = "Refills the Stack in hand",
    max = 1,
    usage = "/more")
    public void more(CommandContext context)
    {
        User user = cuManager.getUser(context.getSender());
        if (user == null)
        {
            invalidUsage(context.getSender(), "core", "&cThis command can only be used by a player!");
        }
        user.getItemInHand().setAmount(64);
        user.sendMessage("basics", "Refilled Stack in Hand!");
    }

    @Command(
    desc = "Repairs your items",
    flags = {@Flag(longName = "all", name = "a")},
    usage = "/repair [-all]") // without item in hand
    public void repair(CommandContext context)
    {
        User sender = cuManager.getUser(context.getSender());
        if (sender == null)
        {
            invalidUsage(context.getSender(), "core", "&cThis command can only be used by a player!");
        }
        if (context.hasFlag("a"))
        {
            List<ItemStack> list = new ArrayList<ItemStack>();
            list.addAll(Arrays.asList(sender.getInventory().getArmorContents()));
            list.addAll(Arrays.asList(sender.getInventory().getContents()));
            int repaired = 0;
            for (ItemStack item : list)
            {
                if (MaterialMatcher.get().isRepairable(item))
                {
                    item.setDurability((short) 0);
                    repaired++;
                }
            }
            if (repaired == 0)
            {
                sender.sendMessage("No items to repair!");//TODO
            }
            else
            {
                sender.sendMessage("", "Repaired %d items!", repaired);//TODO
            }
        }
        else
        {
            ItemStack item = sender.getItemInHand();
            if (MaterialMatcher.get().isRepairable(item))
            {
                item.setDurability((short) 0);
                sender.sendMessage("Item repaired!");//TODO
            }
            else
            {
                sender.sendMessage("Item cannot be repaired!");//TODO
            }
        }
    }

    @Command(
    desc = "Changes the time of a world",
    min = 1, max = 2,
    flags={@Flag(name="a",longName="all")},
    usage = "/time <day|night|dawn|even|<time>> [world] [-all]")
    public void time(CommandContext context)
    {
        long time = 0;
        String timeString = context.getIndexed(0, String.class, null);
        if (timeString.equalsIgnoreCase("day"))
        {
            time = 12 * 1000;
        }
        else if (timeString.equalsIgnoreCase("night"))
        {
            time = 0;
        }
        else if (timeString.equalsIgnoreCase("dawn"))
        {
            time = 6 * 1000;
        }
        else if (timeString.equalsIgnoreCase("even"))
        {
            time = 18 * 1000;
        }
        else
        {
            try
            {
                time = Long.parseLong(timeString);
            }
            catch (NumberFormatException e)
            {
                invalidUsage(context.getSender(), "basics", "The time has to be a Number greater than 0!");
            }
        }
        if (context.hasFlag("a"))
        {
            for (World world : context.getSender().getServer().getWorlds())
            {
                world.setTime(time);
            }
        }
        else
        {
            User sender = cuManager.getUser(context.getSender());
            World world = null;
            if (context.hasIndexed(1))
            {
                String worldname = context.getIndexed(1, String.class, "");
                world = context.getSender().getServer().getWorld(worldname);
                if (world == null)
                {
                    invalidUsage(context.getSender(), "basics", "&cThe World %s does not exist!", context.getString(1));
                    //TODO msg unknown world print worldlist
                }
            }
            else
            {
                if (sender == null)
                {
                    invalidUsage(context.getSender(), "basics", "If not used by a player you have to specify a world!");
                }
            }
            if (world == null)
            {
                world = sender.getWorld();
            }
            world.setTime(time);
            if (sender == null)
            {
                context.getSender().sendMessage(_("", "Time set to %d in world %s", time, world.getName()));
            }
            else
            {
                sender.sendMessage("", "Time set to %d in world %s", time, world.getName());
            }
        }
    }

    @Command(
    desc = "Changes the time for a player",
    min = 1,
    max = 2,
    usage = "/ptime <day|night|dawn|even> [player]")
    public void ptime(CommandContext context)
    {
        long time = 0;
        boolean other;
        boolean reset = false;
        boolean relative = false; //TODO flag for setting this
        String timeString = context.getIndexed(0, String.class, null);
        if (timeString.equalsIgnoreCase("day"))
        {
            time = 12 * 1000;
        }
        else if (timeString.equalsIgnoreCase("night"))
        {
            time = 0;
        }
        else if (timeString.equalsIgnoreCase("dawn"))
        {
            time = 6 * 1000;
        }
        else if (timeString.equalsIgnoreCase("even"))
        {
            time = 18 * 1000;
        }
        else if (timeString.equalsIgnoreCase("reset"))
        {
            reset = true;
        }
        else
        {
            try
            {
                time = Long.parseLong(timeString);
            }
            catch (NumberFormatException e)
            {
                invalidUsage(context.getSender(), "basics", "The time has to be a Number greater than 0!");
            }
        }
        

        User sender = cuManager.getUser(context.getSender());
        User user = sender;
        if (context.hasIndexed(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                invalidUsage(context.getSender(), "core", "&cThe User %s does not exist!", context.getString(0));
            }
            other = true; //TODO permission
        }
        if (reset)
        {
            user.resetPlayerTime();
        }
        else
        {
            user.setPlayerTime(time, relative);
        }
        if (sender == null)
        {
            context.getSender().sendMessage(_("", "Time set to %d for player %s", time, user.getName()));
        }
        else
        {
            sender.sendMessage("", "Time set to %d for %s", time, user.getName());
        }

        
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
    
    public void unlimited(CommandContext context)
    {
        //TODO
    }
}