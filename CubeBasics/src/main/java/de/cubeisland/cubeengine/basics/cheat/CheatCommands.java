package de.cubeisland.cubeengine.basics.cheat;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.Perm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.MaterialMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Anselm Brehme
 */
public class CheatCommands
{
    private UserManager cuManager;

    public CheatCommands(Basics module)
    {
        cuManager = module.getUserManager();
    }

    @Command(
    desc = "Adds an Enchantment to the item in your hand",
    min = 1,
    max = 2,
    flags = {@Flag(longName = "unsafe", name = "u")},
    usage = "<enchantment> [level] [-unsafe]")
    public void enchant(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&cThis command can only be used by a player!");
        ItemStack item = sender.getItemInHand();
        if (item.getType().equals(Material.AIR))
        {
            invalidUsage(context, "basics", "&6ProTip: You cannot enchant your fists!");
        }
        Enchantment ench = null;
        if (context.hasIndexed(1))
        {
            ench = context.getIndexed(0, Enchantment.class, null);
        }
        if (ench == null)
        {
            illegalParameter(context, "basics", "Enchantment not found! Try one of those instead:");
            // TODO list possible enchantments for item in hand
            return;
        }
        int level = ench.getMaxLevel();
        if (context.hasIndexed(2))
        {
            level = context.getIndexed(1, int.class, 0);
            if (level <= 0)
            {
                illegalParameter(context, "basics", "The EnchantmentLevel has to be a Number greater than 0!");
            }
        }
        if (context.hasFlag("u")) // Add unsafe enchantment //TODO permission
        {
            if (Perm.COMMAND_ENCHANT_UNSAFE.isAuthorized(sender))
            {
                item.addUnsafeEnchantment(ench, level);
                context.sendMessage("bascics", null, "&aAdded unsafe Enchantment: &6%s&a to your item!", ench.toString()); //TODO use other than ench.toString AND add level
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
                    context.sendMessage("bascics", "&aAdded Enchantment: &6%s&a to your item!", ench.toString()); //TODO use other than ench.toString  AND add level
                    return;
                }
                illegalParameter(context, "basics", "This enchantmentlevel is not allowed!");
            }
            illegalParameter(context, "basics", "This enchantment is not allowed for this item!");
        }
    }

    @Command(
    desc = "Refills your hunger bar",
    max = 1,
    flags = {@Flag(longName = "all", name = "a")},
    usage = "[player]")
    public void feed(CommandContext context)
    {
        if (context.hasFlag("a"))
        {
            Player[] players = context.getSender().getServer().getOnlinePlayers();
            for (Player player : players)
            {
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.setExhaustion(0);
            }
            //TODO msg fed all
        }
        else
        {
            User sender = context.getSenderAsUser();
            User user = sender;
            boolean other = false;
            if (context.hasIndexed(0))
            {
                user = context.getUser(0, true);
                other = true;
            }
            else
            {
                if (sender == null)
                {
                    invalidUsage(context, "basics", "&cDon't feed the troll!");//TODO if not player given and
                }
            }
            user.setFoodLevel(20);
            user.setSaturation(20);
            user.setExhaustion(0);
            if (other)
            {
                context.sendMessage("basics", "&6Feeded %s", user.getName());
                user.sendMessage("basics", "&6You got fed by %s", sender.getName());
            }
            else
            {
                context.sendMessage("basics", "&6You are now fed!");
            }
        }
    }

    @Command(
    names = {"gamemode", "gm"},
    max = 2,
    desc = "Changes the gamemode",
    usage = "<gamemode> [player]")
    public void gamemode(CommandContext context)
    {
        boolean changeOther = false;

        User sender = context.getSenderAsUser();
        User user = sender;
        if (user == null)
        {
            invalidUsage(context, "basics", "&cYou do not not have any gamemode!");
        }
        if (context.hasIndexed(1))
        {
            user = context.getUser(1, true);
            changeOther = true;
        }
        if (!Perm.COMMAND_GAMEMODE_OTHER.isAuthorized(sender))
        {
            denyAccess(context, "basics", "You do not have permission to change the gamemode of an other player!");
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
            context.sendMessage("basics", "You changed the gamemode of %s to %s", user.getName(), _(sender, "basics", user.getGameMode().toString()));
            // TODO later notify user who changed if flag set (permission)
            user.sendMessage("basics", "Your Gamemode has been changed to %s", _(user, "basics", user.getGameMode().toString()));
        }
        else
        {
            context.sendMessage("basics", "You changed your gamemode to %s", _(user, "basics", user.getGameMode().toString()));
        }
    }

    @Command(
    desc = "Heals a Player",
    max = 1,
    usage = "[player]")
    public void heal(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        if (sender == null)
        {
            invalidUsage(context, "basics", "&cOnly time can heal your wounds!");//TODO if not player given and
            //TODO all flag
        }
        User user = sender;
        boolean other = false;
        if (context.hasIndexed(0))
        {
            user = context.getUser(0, true);
            other = true;
        }
        user.setFoodLevel(20);
        user.setSaturation(20);
        user.setExhaustion(0);
        if (other)
        {
            context.sendMessage("basics", "&6Healed %s", user.getName());
            user.sendMessage("basics", "&6You got healed by %s", sender.getName());
        }
        else
        {
            context.sendMessage("basics", "&6You are now healed!");
        }
    }

    @Command(
    desc = "Gives the specified Item to a player",
    flags = {@Flag(name = "b", longName = "blacklist")},
    min = 2, max = 3,
    usage = "<player> <material[:data]> [amount] [-blacklist]")
    public void give(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        User user = context.getUser(0,true);
        ItemStack item = context.getIndexed(1, ItemStack.class, null);
        if (item == null)
        {
            illegalParameter(context, "core", "&cUnknown Item: %s!", context.getString(1));
            //TODO invalidArgumentException or smth like that  with invalidItem() <- I do need this quite often
        }
        if (context.hasFlag("b") && Perm.COMMAND_GIVE_BLACKLIST.isAuthorized(sender))
        {
            if (1 == 0) // TODO Blacklist
            {
                denyAccess(context, "basics", "&cThis item is blacklisted!");
            }
        }
        int amount = item.getMaxStackSize();
        if (context.hasIndexed(3))
        {
            amount = context.getIndexed(2, int.class, 0);
            if (amount == 0)
            {
                illegalParameter(context, "basics", "&cThe amount has to be a number greater than 0!");
            }
        }
        item.setAmount(amount);
        
        user.getInventory().addItem(item);
        user.updateInventory();
        context.sendMessage("basics", "You gave %s %d %s", user.getName(), item.toString(), amount);
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
    usage = "<material[:data]> [amount] [-blacklist]")
    public void item(CommandContext context)
    {
        User sender = context.getSenderAsUser(true);
        ItemStack item = context.getIndexed(0, ItemStack.class, null);
        if (item == null)
        {
            illegalParameter(context, "core", "&cUnknown Item: %s!", context.getString(1));
        }
        if (context.hasFlag("b") && Perm.COMMAND_ITEM_BLACKLIST.isAuthorized(sender))
        {
            if (1 == 0) // TODO Blacklist
            {
                denyAccess(context, "basics", "&cThis item is blacklisted!");
            }
        }
        int amount = item.getMaxStackSize();
        if (context.hasIndexed(2))
        {
            amount = context.getIndexed(1, int.class, 0);
            if (amount == 0)
            {
                illegalParameter(context, "basics", "The amount has to be a Number greater than 0!");
            }
        }
        item.setAmount(amount);
        sender.getInventory().addItem(item);
        sender.updateInventory();
        sender.sendMessage("basics", "Received: %d %s ", item.toString(), amount); // TODO item.toString is no good
    }

    @Command(
    desc = "Refills the Stack in hand",
    max = 0)
    public void more(CommandContext context)
    {
        User sender = context.getSenderAsUser(true);
        sender.getItemInHand().setAmount(64);
        sender.sendMessage("basics", "Refilled Stack in Hand!");
    }

    @Command(
    desc = "Repairs your items",
    flags = {@Flag(longName = "all", name = "a")},
    usage = "/repair [-all]") // without item in hand
    public void repair(CommandContext context)
    {
        User sender = context.getSenderAsUser(true);
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

    private enum Time
    {
        DAY(6000, "day", "noon"),
        NIGHT(18000, "night", "midnight"),
        DAWN(0, "dawn", "morning"),
        DUSK(12000, "dusk", "even");

        private static final HashMap<String,Time> times = new  HashMap<String, Time>();
        protected String[] names;
        protected long longTime;
        
        static
        {
            for (Time time : values())
            {
                for (String name : time.names)
                {
                    times.put(name, time);
                }
            }
        }
        
        private Time(long longTime, String... names)
        {
            this.names = names;
            this.longTime = longTime;
        }
        
        public static Long matchTime(String s)
        {
            if (s == null)
            {
                return null;
            }
            Time time = times.get(s);
            if (time != null)
            {
                return time.longTime;
            }
            try //TODO time as 12:00 4pm/am etc.
            {
                return Long.parseLong(s); // this is time in ticks
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
    }

    
    @Command(
    desc = "Changes the time of a world",
    min = 1, max = 2,
    flags={@Flag(name="a",longName="all")},
    usage = "<day|night|dawn|even|<time>> [world] [-all]")
    public void time(CommandContext context)
    { //TODO time matcher to make this easier!
        //TODO change output time set to %d to day|night etc..
        String timeString = context.getIndexed(0, String.class, null);
        Long time = Time.matchTime(timeString);
        if (time == null)
        {
            illegalParameter(context, "basics", "Invalid Time format! Use those instead:");//TODO show usage hereafter
        }
        if (context.hasFlag("a"))
        {
            for (World world : context.getSender().getServer().getWorlds())
            {
                world.setTime(time);
            }
            context.getSender().sendMessage(_("", "Time set to %d in all worlds",time)); //TODO translate for user too
        }
        else
        {
            User sender = context.getSenderAsUser();
            World world = null;
            if (context.hasIndexed(1))
            {
                String worldname = context.getIndexed(1, String.class, "");
                world = context.getSender().getServer().getWorld(worldname);
                if (world == null)
                {
                    illegalParameter(context, "basics", "&cThe World %s does not exist!", context.getString(1));
                    //TODO msg unknown world print worldlist
                }
            }
            else
            {
                if (sender == null)
                {
                    invalidUsage(context, "basics", "If not used by a player you have to specify a world!");
                }
            }
            if (world == null)
            {
                world = sender.getWorld();
            }
            world.setTime(time);
            context.sendMessage("basics", "Time set to %d in world %s", time, world.getName());
        }
    }

    @Command(
    desc = "Changes the time for a player",
    min = 1,
    max = 2,
    usage = "<day|night|dawn|even> [player]")
    public void ptime(CommandContext context)
    {
        Long time = 0L;
        boolean other = false;
        boolean reset = false;
        boolean relative = false; //TODO flag for setting this
        String timeString = context.getIndexed(0, String.class, null);

        if (timeString.equalsIgnoreCase("reset"))
        {
            reset = true;
        }
        else
        {
            time = Time.matchTime(timeString);
            if (time == null)
            {
                invalidUsage(context, "basics", "Invalid Time format! Use those instead:");//TODO show usage hereafter
            }
        }
        User sender = context.getSenderAsUser();
        User user = sender;
        if (context.hasIndexed(1))
        {
            user = context.getUser(1, true);
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
        context.sendMessage("basics", "Time set to %d for %s", time, user.getName());
    }
    
    @Command(
    desc = "The user can use unlimited items",
    max = 1,
    usage = "[on|off]")
    public void unlimited(CommandContext context)
    {
        //TODO with param
        User sender = context.getSenderAsUser(true);
        Object bln = sender.getAttribute("unlimitedItems");
        if (bln == null)
        {
            sender.setAttribute("unlimitedItems", true);
        }
        else
        {
            sender.removeAttribute("unlimitedItems");
        }
    }
}