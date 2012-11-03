package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.EnchantMatcher;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

public class ItemCommands
{
    private Basics basics;

    public ItemCommands(Basics basics)
    {
        this.basics = basics;

    }

    @Command(
        desc = "Looks up an item for you!",
    max = 1,
    usage = "<item>")
    public void itemDB(CommandContext context)
    {
        ItemStack item = MaterialMatcher.get().matchItemStack(context.getString(0));
        if (item != null)
        {
            context.sendMessage("basics", "Found %s (%d:%d)", MaterialMatcher.get().getNameFor(item), item.getType().getId(), item.getDurability());
        }
        else
        {
            context.sendMessage("basics", "Could not find any item named %s", context.getString(0));
        }
    }

    @Command(
    desc = "Gives a kit of items.",
    usage = "<kitname> [player]",
    min = 1, max = 2,
    flags =
    {
        @Flag(longName = "all", name = "a")
    })
    public void kit(CommandContext context)
    {
        String kitname = context.getString(0);
        User user;
        Kit kit = null; //TODO getKitFromConfig
        if (kit == null)
        {
            illegalParameter(context, "basics", "Kit %s not found!", kitname);
        }
        if (context.hasIndexed(1))
        {
            user = context.getUser(1);
        }
        else
        {
            user = context.getSenderAsUser();
        }
        if (user == null)
        {
            illegalParameter(context, "basics", "User not found!");
        }
        boolean result = kit.give(context.getSender(), user);
        if (result)
        {
            context.sendMessage("basics", "%s does not have enough space for the %s kit ", user.getName(), kitname);
        }
        else if (user.getName().equals(context.getSender().getName()))
        {
            context.sendMessage("basics", "Received the %s kit", kitname);
        }
        else
        {
            context.sendMessage("basics", "You gave %s the %s kit", user.getName(), kitname);
            user.sendMessage("basics", "Received the %s kit. Enjoy it!", kitname);
        }
    }

    @Command(
    desc = "Changes the display name of the item in your hand.",
    usage = "<name>",
    min = 1)
    public void rename(CommandContext context)
    {
        String name = context.getStrings(0);
        if (BukkitUtils.renameItemStack(context.getSenderAsUser("basics", "&eTrying to give your toys a name?").getItemInHand(), name))
        {
            context.sendMessage("basics", "&aYou now hold &6%s &ain your hands!", name);
        }
        else
        {
            context.sendMessage("basics", "&cRenaming failed!");
        }
    }

    @Command(
    names =
    {
        "headchange", "skullchange"
    },
    desc = "Changes a skull to a players skin.",
    usage = "<name>",
    min = 1)
    public void headchange(CommandContext context)
    {
        //TODO later listener to drop the custom heads
        String name = context.getString(0);
        User sender = context.getSenderAsUser("basics", "&eTrying to give your toys a name?");
        CraftItemStack changedHead = BukkitUtils.changeHead(sender.getItemInHand(), name);
        if (changedHead != null)
        {
            context.sendMessage("basics", "&aYou now hold &6%s's &ahead in your hands!", name);
            sender.setItemInHand(changedHead);
            sender.updateInventory();
        }
        else
        {
            context.sendMessage("basics", "&cYou are not holding a head.");
        }
    }

    @Command(
    desc = "The user can use unlimited items",
    max = 1,
    usage = "[on|off]")
    public void unlimited(CommandContext context)
    {
        User sender = context.
            getSenderAsUser("core", "&cThis command can only be used by a player!");
        if (context.hasIndexed(0))
        {
            if (context.getString(0).equalsIgnoreCase("on"))
            {
                sender.setAttribute(basics, "unlimitedItems", true);
                sender.
                    sendMessage("basics", "You now have unlimited items to build!");
            }
            else
            {
                if (context.getString(0).equalsIgnoreCase("off"))
                {
                    sender.removeAttribute(basics, "unlimitedItems");
                    sender.sendMessage("basics", "You now no longer have unlimited items to build!");
                }
                else
                {
                    invalidUsage(context);
                }
            }
        }
        else
        {
            Object bln = sender.getAttribute(basics, "unlimitedItems");
            if (bln == null)
            {
                sender.setAttribute(basics, "unlimitedItems", true);
                context.sendMessage("basics", "You now have unlimited items to build!");
            }
            else
            {
                sender.removeAttribute(basics, "unlimitedItems");
                context.sendMessage("basics", "You now no longer have unlimited items to build!");
            }
        }
    }

    @Command(
    desc = "Adds an Enchantment to the item in your hand",
    min = 1,
    max = 2,
    flags =
    {
        @Flag(longName = "unsafe", name = "u")
    },
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
                illegalParameter(context, "basics", "Enchantment %s not found! Try one of those instead:%s", context.
                    getString(0), possibleEnchs);
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
                context.
                    sendMessage("basics", "&aAdded unsafe Enchantment: &6%s %d&a to your item!", EnchantMatcher.
                    get().getNameFor(ench), level);
                return;
            }
            denyAccess(sender, "basics", "You are not allowed to add unsafe enchantments!");
        }
        else
        {
            if (ench.canEnchantItem(item))
            {
                if ((level >= ench.getStartLevel()) && (level <= ench.
                    getMaxLevel()))
                {
                    item.addUnsafeEnchantment(ench, level);
                    context.
                        sendMessage("bascics", "&aAdded Enchantment: &6%s %d&a to your item!", EnchantMatcher.
                        get().getNameFor(ench), level);
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
                    sb.append("\n").append(EnchantMatcher.get().
                        getNameFor(enchantment));
                    first = false;
                }
                else
                {
                    sb.append(",").append(EnchantMatcher.get().
                        getNameFor(enchantment));
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
    desc = "Gives the specified Item to a player",
    flags =
    {
        @Flag(name = "b", longName = "blacklist")
    },
    min = 2, max = 3,
    usage = "<player> <material[:data]> [amount] [-blacklist]")
    public void give(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            invalidUsage(context, "core", "User not found!");
        }
        ItemStack item = context.getIndexed(1, ItemStack.class, null);
        if (item == null)
        {
            illegalParameter(context, "core", "&cUnknown Item: %s!", context.
                getString(1));
        }
        if (context.hasFlag("b") && BasicsPerm.COMMAND_GIVE_BLACKLIST.
            isAuthorized(context.getSender()))
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
        String matname = MaterialMatcher.get().getNameFor(item);
        context.sendMessage("basics", "You gave %s %d %s", user.getName(), amount, matname);
        user.sendMessage("%s just gave you %d %s", context.getSender().getName(), amount, matname);
    }

    @Command(
    names =
    {
        "item", "i"
    },
    desc = "Gives the specified Item to you",
    max = 2,
    min = 1,
    flags =
    {
        @Flag(longName = "blacklist", name = "b")
    },
    usage = "<material[:data]> [amount] [-blacklist]")
    public void item(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&cThis command can only be used by a player!");
        ItemStack item = context.getIndexed(0, ItemStack.class, null);
        if (item == null)
        {
            illegalParameter(context, "core", "&cUnknown Item: %s!", context.
                getString(1));
        }
        if (context.hasFlag("b") && BasicsPerm.COMMAND_ITEM_BLACKLIST.
            isAuthorized(sender))
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
        sender.sendMessage("basics", "&eReceived: %d %s ", amount, MaterialMatcher.get().getNameFor(item));
    }

    @Command(
    desc = "Refills the Stack in hand",
    usage = "[-a]",
    flags =
    {
        @Flag( longName = "all", name = "a")
    },
    max = 0)
    public void more(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&cThis command can only be used by a player!");
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR)
        {
            invalidUsage(context, "basics", "More nothing is still nothing.");
        }
        if (context.hasFlag("a"))
        {
            for (ItemStack item : sender.getInventory().getContents())
            {
                if (item.getType() != Material.AIR)
                {
                    item.setAmount(64);
                }
            }
        }
        else
        {
            sender.getItemInHand().setAmount(64);
            sender.sendMessage("basics", "Refilled Stack in Hand!");
        }
    }

    @Command(
    desc = "Repairs your items",
    flags =
    {
        @Flag(longName = "all", name = "a")
    },
    usage = "[-all]") // without item in hand
    public void repair(CommandContext context)
    {
        User sender = context.
            getSenderAsUser("core", "&cThis command can only be used by a player!");
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
                    item.setDurability((short)0);
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
                    sender.sendMessage("basics", "No need to repair this!");
                    return;
                }
                item.setDurability((short)0);
                sender.sendMessage("basics", "Item repaired!");
            }
            else
            {
                sender.sendMessage("basics", "Item cannot be repaired!");
            }
        }
    }
    
    @Command(
    names =
    {
        "pt", "powertool"
    },
    desc = "Binds a command to the item in hand.",
    usage = "<command> [arguments]",
    min = 1, max = 2,
    flags =
    {
        @Flag(longName = "all", name = "a")
    })
    public void powertool(CommandContext context)
    {
        //TODO listener
        //TODO how to save this in db??? map of ItemStack -> String
        context.sendMessage("not implemented Yet");
    }
}
