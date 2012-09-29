package de.cubeisland.cubeengine.basics.cheat;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.EnchantMatcher;
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
    private UserManager uM;

    public CheatCommands(Basics module)
    {
        uM = module.getUserManager();
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
        Enchantment ench = context.getIndexed(0, Enchantment.class, null);
        if (ench == null)
        {
            String possibleEnchs = this.getPossibleEnchantments(item);
            if (possibleEnchs != null)
            {
                illegalParameter(context, "basics", "Enchantment %s not found! Try one of those instead:%s", context.getString(0), possibleEnchs);
            }
            else
            {
                illegalParameter(context, "basics", "&cYou can not enchant this item!");
            }
        }
        int level = ench.getMaxLevel();
        if (context.hasIndexed(1))
        {
            level = context.getIndexed(1, int.class, 0);
            if (level <= 0)
            {
                illegalParameter(context, "basics", "The EnchantmentLevel has to be a Number greater than 0!");
            }
        }
        if (context.hasFlag("u"))
        {
            if (BasicsPerm.COMMAND_ENCHANT_UNSAFE.isAuthorized(sender))
            {
                item.addUnsafeEnchantment(ench, level);
                context.sendMessage("basics", "&aAdded unsafe Enchantment: &6%s %d&a to your item!", EnchantMatcher.get().getNameFor(ench), level);
                return;
            }
            denyAccess(sender, "basics", "You are not allowed to add unsafe enchantments!");
        }
        else
        {
            if (ench.canEnchantItem(item))
            {
                if ((level >= ench.getStartLevel()) && (level <= ench.getMaxLevel()))
                {
                    item.addUnsafeEnchantment(ench, level);
                    context.sendMessage("bascics", "&aAdded Enchantment: &6%s %d&a to your item!", EnchantMatcher.get().getNameFor(ench), level);
                    return;
                }
                illegalParameter(context, "basics", "This enchantmentlevel is not allowed!");
            }
            String possibleEnchs = this.getPossibleEnchantments(item);
            if (possibleEnchs != null)
            {
                illegalParameter(context, "basics", "This enchantment is not allowed for this item! Try one of those instead:&e%s", possibleEnchs);
            }
            else
            {
                illegalParameter(context, "basics", "&cYou can not enchant this item!");
            }
        }
    }

    private String getPossibleEnchantments(ItemStack item)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Enchantment enchantment : Enchantment.values())
        {
            if (enchantment.canEnchantItem(item))
            {
                if (first)
                {
                    sb.append("\n").append(EnchantMatcher.get().getNameFor(enchantment));
                    first = false;
                }
                else
                {
                    sb.append(",").append(EnchantMatcher.get().getNameFor(enchantment));
                }
                        
            }
        }
        if (sb.length() == 0)
        {
            return null;
        }
        return sb.toString();
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
            context.sendMessage("basics","You made everyone fat!");
            uM.broadast("basics", "%s shared food with everyone.", context.getSender().getName());
        }
        else
        {
            User sender = context.getSenderAsUser();
            User user = sender;
            boolean other = false;
            if (context.hasIndexed(0))
            {
                user = context.getUser(0);
                if (user == null)
                {
                    invalidUsage(context, "basics", "User not found!");
                }
                other = true;
            }
            else if (sender == null)
            {
                invalidUsage(context, "basics", "&cDon't feed the troll!");
            }
            user.setFoodLevel(20);
            user.setSaturation(20);
            user.setExhaustion(0);
            if (other)
            {
                context.sendMessage("basics", "&6Feeded %s", user.getName());
                user.sendMessage("basics", "&6You got fed by %s", context.getSender().getName());
            }
            else
            {
                context.sendMessage("basics", "&6You are now fed!");
            }
        }
    }
    
    @Command(
    desc = "Heals a Player",
    max = 1,
    flags = {@Flag(longName = "all", name = "a")},
    usage = "[player]")
    public void heal(CommandContext context)
    {
        if (context.hasFlag("a"))
        {
            Player[] players = context.getSender().getServer().getOnlinePlayers();
            for (Player player : players)
            {
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.setExhaustion(0);
            }
            context.sendMessage("basics", "You healed everyone!");
            uM.broadast("basics", "%s healed every player.", context.getSender().getName());
        }
        else
        {
            User sender = context.getSenderAsUser();
            User user = sender;
            boolean other = false;
            if (context.hasIndexed(0))
            {
                if (user == null)
                {
                    invalidUsage(context, "basics", "User not found!");
                }
                other = true;
            }
            else
            {
                if (sender == null)
                {
                    invalidUsage(context, "basics", "&cOnly time can heal your wounds!");
                }
            }
            user.setHealth(user.getMaxHealth());
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
        User user = sender ;
        if (user == null)
        {
            invalidUsage(context, "basics", "&cYou do not not have any gamemode!");
        }
        if (context.hasIndexed(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                invalidUsage(context, "basics", "User not found!");
            }
            changeOther = true;
        }
        if (!BasicsPerm.COMMAND_GAMEMODE_OTHER.isAuthorized(sender))
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
            user.sendMessage("basics", "Your Gamemode has been changed to %s", _(user, "basics", user.getGameMode().toString()));
        }
        else
        {
            context.sendMessage("basics", "You changed your gamemode to %s", _(user, "basics", user.getGameMode().toString()));
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
        User user = context.getUser(0);
        if (user == null)
        {
            invalidUsage(context, "basics", "User not found!");
        }
        ItemStack item = context.getIndexed(1, ItemStack.class, null);
        if (item == null)
        {
            illegalParameter(context, "core", "&cUnknown Item: %s!", context.getString(1));
        }
        if (context.hasFlag("b") && BasicsPerm.COMMAND_GIVE_BLACKLIST.isAuthorized(context.getSender()))
        {
            if (1 == 0) // TODO Blacklist
            {
                denyAccess(context, "basics", "&cThis item is blacklisted!");
            }
        }
        int amount = item.getMaxStackSize();
        if (context.hasIndexed(2))
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
        context.sendMessage("basics", "You gave %s %d %s", user.getName(), amount, item.toString());
        user.sendMessage("%s just gave you %d %s", context.getSender().getName(), item.toString(), amount);
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
        User sender = context.getSenderAsUser("core", "&cThis command can only be used by a player!");
        ItemStack item = context.getIndexed(0, ItemStack.class, null);
        if (item == null)
        {
            illegalParameter(context, "core", "&cUnknown Item: %s!", context.getString(1));
        }
        if (context.hasFlag("b") && BasicsPerm.COMMAND_ITEM_BLACKLIST.isAuthorized(sender))
        {
            if (1 == 0) // TODO Blacklist
            {
                denyAccess(context, "basics", "&cThis item is blacklisted!");
            }
        }
        int amount = item.getMaxStackSize();
        if (context.hasIndexed(1))
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
        sender.sendMessage("basics", "Received: %d %s ", amount, item.toString()); // TODO item.toString is no good
    }

    @Command(
    desc = "Refills the Stack in hand",
    max = 0)
    public void more(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&cThis command can only be used by a player!");
        sender.getItemInHand().setAmount(64);
        sender.sendMessage("basics", "Refilled Stack in Hand!");
    }

    @Command(
    desc = "Repairs your items",
    flags = {@Flag(longName = "all", name = "a")},
    usage = "/repair [-all]") // without item in hand
    public void repair(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&cThis command can only be used by a player!");
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
                sender.sendMessage("basics", "No items to repair!");
            }
            else
            {
                sender.sendMessage("basics", "Repaired %d items!", repaired);
            }
        }
        else
        {
            ItemStack item = sender.getItemInHand();
            if (MaterialMatcher.get().isRepairable(item))
            {
                if (item.getDurability() == 0)
                {
                    sender.sendMessage("basics","No need to repair this!");
                    return;
                }
                item.setDurability((short) 0);
                sender.sendMessage("basics","Item repaired!");
            }
            else
            {
                sender.sendMessage("basics","Item cannot be repaired!");
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
        private static final HashMap<Long,String> timeNames = new  HashMap<Long, String>();
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
                timeNames.put(time.longTime, time.names[0]);
            }
        }
        
        private Time(long longTime, String... names)
        {
            this.names = names;
            this.longTime = longTime;
        }
        
        public static String getTimeName(Long time)
        {
            return timeNames.get(time);
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
    {
        //TODO change output time set to %d to day|night etc..
        String timeString = context.getIndexed(0, String.class, null);
        Long time = Time.matchTime(timeString);
        if (time == null)
        {
            illegalParameter(context, "basics", "Invalid Time format!");
        }
        if (context.hasFlag("a"))
        {
            for (World world : context.getSender().getServer().getWorlds())
            {
                world.setTime(time);
            }
            String timeName = Time.getTimeName(time);
            if (timeName == null)
            {
                context.sendMessage("basics", "Time set to %d in all worlds",time);
            }
            else
            {
                context.sendMessage("basics", "Time set to %s in all worlds",timeName);
            }
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
                    List<World> worlds = context.getSender().getServer().getWorlds();
                    StringBuilder sb = new StringBuilder();
                    for (World w : worlds)
                    {
                        sb.append(" ").append(w.getName());
                    }
                    illegalParameter(context, "basics", "&cThe World %s does not exist!\nUse one of those:%s", context.getString(1), sb.toString());
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
            String timeName = Time.getTimeName(time);
            if (timeName == null)
            {
                context.sendMessage("basics", "Time set to %d in world %s", time, world.getName());
            }
            else
            {
                context.sendMessage("basics", "Time set to %s in world %s", timeName, world.getName());
            }
        }
    }

    @Command(
    desc = "Changes the time for a player",
    min = 1,
    max = 2,
    flags = { @Flag(longName="relative",name="rel")},
    usage = "<day|night|dawn|even> [player]")
    public void ptime(CommandContext context)
    {
        Long time = 0L;
        boolean other = false;
        boolean reset = false;
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
                invalidUsage(context, "basics", "Invalid Time format!");
            }
        }
        User sender = context.getSenderAsUser();
        User user = sender;
        if (context.hasIndexed(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                invalidUsage(context, "basics", "User not found!");
            }
            if (!BasicsPerm.COMMAND_PTIME_OTHER.isAuthorized(context.getSender()))
            {
                denyAccess(context, "basics", "&cYou are not allowed to change the time of other players!");
            }
            other = true;
        }
        if (reset)
        {
            user.resetPlayerTime();
            context.sendMessage("basics", "Resetted the time for %s!", user.getName());
            if (other)
            {
                user.sendMessage("basics", "Your time was resetted!");
            }
        }
        else
        {
            user.setPlayerTime(time, context.hasFlag("rel"));
            String timeName = Time.getTimeName(time);
            if (timeName == null)
            {
                context.sendMessage("basics", "Time set to %d for %s", time, user.getName());
            }
            else
            {
                context.sendMessage("basics", "Time set to %s for %s", timeName, user.getName());
            }
            if (other)
            {
                if (timeName == null)
                {
                    user.sendMessage("basics", "Your time was set to %d!", time);
                }
                else
                {
                    user.sendMessage("basics", "Your time was set to %s!", timeName);
                }
            }
        }
    }
    
    @Command(
    desc = "The user can use unlimited items",
    max = 1,
    usage = "[on|off]")
    public void unlimited(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&cThis command can only be used by a player!");
        if (context.hasIndexed(0))
        {
            if (context.getString(0).equalsIgnoreCase("on"))
            {
                sender.setAttribute("unlimitedItems", true);
                sender.sendMessage("basics", "You now have unlimited items to build!");
            }
            else if (context.getString(0).equalsIgnoreCase("off"))
            {
                sender.removeAttribute("unlimitedItems");
                sender.sendMessage("basics", "You now no longer have unlimited items to build!");
            }
            else
            {
                invalidUsage(context);
            }
        }
        else
        {
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
}