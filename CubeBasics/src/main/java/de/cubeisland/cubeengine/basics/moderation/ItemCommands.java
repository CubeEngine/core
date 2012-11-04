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
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import static de.cubeisland.cubeengine.core.i18n.I18n._;

/**
 * item-related commands
 * /itemdb
 * /kit  //TODO
 * /rename
 * /headchange
 * /unlimited
 * /enchant
 * /give
 * /item
 * /more
 * /repair
 * /powertool
 */
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
        usage = "[item]")
    public void itemDB(CommandContext context)
    {
        if (context.hasIndexed(0))
        {
            ItemStack item = MaterialMatcher.get().matchItemStack(context.getString(0));
            if (item != null)
            {
                context.sendMessage("basics", "&aMatched &e%s &f(&e%d&f:&e%d&f) &afor &f%s",
                    MaterialMatcher.get().getNameFor(item), item.getType().getId(), item.getDurability(), context.getString(0));
            }
            else
            {
                context.sendMessage("basics", "&cCould not find any item named &e%s&c!", context.getString(0));
            }
        }
        else
        {
            User sender = context.getSenderAsUser("basics", "&cYou need 1 parameter!");
            if (sender.getItemInHand().getType().equals(Material.AIR))
            {
                invalidUsage(context, "basics", "&eYou hold nothing in your hands!");
            }
            else
            {
                context.sendMessage("basics", "&aThe Item in your hand is: &e%s &f(&e%d&f:&e%d&f)",
                    MaterialMatcher.get().getNameFor(sender.getItemInHand()),
                    sender.getItemInHand().getType().getId(),
                    sender.getItemInHand().getDurability());
            }
        }
    }

    @Command(
        desc = "Gives a kit of items.",
        usage = "<kitname> [player]",
        min = 1, max = 2,
        flags = { @Flag(longName = "all", name = "a") })
    public void kit(CommandContext context)
    {
        //TODO this needs the converters
        String kitname = context.getString(0);
        User user;
        Kit kit = null; //TODO getKitFromConfig
        if (kit == null)
        {
            paramNotFound(context, "basics", "&cKit &6%s &cnot found!", kitname);
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
            paramNotFound(context, "basics", "&cUser &2%s &cnot found!", context.getString(0));
        }
        boolean result = kit.give(context.getSender(), user);
        if (result)
        {
            context.sendMessage("basics", "&2%s &cdoes not have enough space for the &6%s &ckit!", user.getName(), kitname);
        }
        else if (user.getName().equals(context.getSender().getName()))
        {
            context.sendMessage("basics", "&aReceived the &6%s &akit!", kitname);
        }
        else
        {
            context.sendMessage("basics", "&aYou gave &2%s &athe &6%s &akit!", user.getName(), kitname);
            user.sendMessage("basics", "&aReceived the &6%s &akit. Enjoy it!", kitname);
        }
    }

    @Command(
        desc = "Changes the display name of the item in your hand.",
        usage = "<name>",
        min = 1)
    public void rename(CommandContext context)
    {
        String name = context.getStrings(0);
        if (BukkitUtils.renameItemStack(context.getSenderAsUser("basics", "&cTrying to give your &etoys &ca name?").getItemInHand(), name))
        {
            context.sendMessage("basics", "&aYou now hold &6%s &ain your hands!", name);
        }
        else
        {
            context.sendMessage("basics", "&cRenaming failed!");
        }
    }

    @Command(
        names = { "headchange", "skullchange" },
        desc = "Changes a skull to a players skin.",
        usage = "<name>",
        min = 1)
    public void headchange(CommandContext context)
    {
        //TODO later listener to drop the custom heads
        String name = context.getString(0);
        User sender = context.getSenderAsUser("basics", "&cTrying to give your &etoys &ca name?");
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
        User sender = context.getSenderAsUser("core", "&cThis command can only be used by a player!");
        boolean unlimited = false;
        if (context.hasIndexed(0))
        {
            if (context.getString(0).equalsIgnoreCase("on"))
            {
                unlimited = true;
            }
            else if (context.getString(0).equalsIgnoreCase("off"))
            {
                unlimited = false;
            }
            else
            {
                invalidUsage(context, "basics", "&eInvalid parameter! Use &aon &eor %coff&e!");
            }
        }
        else
        {
            Object bln = sender.getAttribute(basics, "unlimitedItems");
            if (bln == null)
            {
                unlimited = true;
            }
            else
            {
                unlimited = false;
            }
        }
        if (unlimited)
        {
            sender.setAttribute(basics, "unlimitedItems", true);
            context.sendMessage("basics", "&aYou now have unlimited items to build!");
        }
        else
        {
            sender.removeAttribute(basics, "unlimitedItems");
            context.sendMessage("basics", "&eYou now no longer have unlimited items to build!");
        }
    }

    @Command(
        desc = "Adds an Enchantment to the item in your hand",
        max = 2,
        flags = { @Flag(longName = "unsafe", name = "u") },
        usage = "<enchantment> [level] [-unsafe]")
    public void enchant(CommandContext context)
    {
        if (!context.hasIndexed(0))
        {
            context.sendMessage("&aFollowing Enchantments are availiable:\n%s", this.getPossibleEnchantments(null));
            return;
        }
        User sender = context.getSenderAsUser("core", "&eWant to be Harry Potter?");
        ItemStack item = sender.getItemInHand();
        if (item.getType().equals(Material.AIR))
        {
            blockCommand(context, "basics", "&6ProTip: &eYou cannot enchant your fists!");
        }
        Enchantment ench = context.getIndexed(0, Enchantment.class, null);
        if (ench == null)
        {
            String possibleEnchs = this.getPossibleEnchantments(item);
            if (possibleEnchs != null)
            {
                blockCommand(context, "basics", "&cEnchantment &6%s &cnot found! Try one of those instead:\n%s", context.
                    getString(0), possibleEnchs);
            }
            else
            {
                blockCommand(context, "basics", "&cYou can not enchant this item!");
            }
        }
        int level = ench.getMaxLevel();
        if (context.hasIndexed(1))
        {
            level = context.getIndexed(1, int.class, 0);
            if (level <= 0)
            {
                illegalParameter(context, "basics", "&cThe enchantment-level has to be a number greater than 0!");
            }
        }
        if (context.hasFlag("u"))
        {
            if (BasicsPerm.COMMAND_ENCHANT_UNSAFE.isAuthorized(sender))
            {
                item.addUnsafeEnchantment(ench, level);
                context.sendMessage("basics", "&aAdded unsafe enchantment: &6%s %d &ato your item!",
                    EnchantMatcher.get().getNameFor(ench), level);
                return;
            }
            denyAccess(sender, "basics", "&cYou are not allowed to add unsafe enchantments!");
        }
        else
        {
            if (ench.canEnchantItem(item))
            {
                if ((level >= ench.getStartLevel()) && (level <= ench.
                    getMaxLevel()))
                {
                    item.addUnsafeEnchantment(ench, level);
                    context.sendMessage("bascics", "&aAdded enchantment: &6%s %d &ato your item!", EnchantMatcher.get().getNameFor(ench), level);
                    return;
                }
                blockCommand(context, "basics", "&cThis enchantment-level is not allowed!");
            }
            String possibleEnchs = this.getPossibleEnchantments(item);
            if (possibleEnchs != null)
            {
                blockCommand(context, "basics", "&cThis enchantment is not allowed for this item!\n&eTry one of those instead:\n%s", possibleEnchs);
            }
            else
            {
                blockCommand(context, "basics", "&cYou can not enchant this item!");
            }
        }
    }

    private String getPossibleEnchantments(ItemStack item)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Enchantment enchantment : Enchantment.values())
        {
            if (item == null || enchantment.canEnchantItem(item))
            {
                if (first)
                {
                    sb.append("&e").append(EnchantMatcher.get().getNameFor(enchantment));
                    first = false;
                }
                else
                {
                    sb.append("&f, &e").append(EnchantMatcher.get().getNameFor(enchantment));
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
            paramNotFound(context, "core", "&cUser &2%s &cnot found!", context.getString(0));
        }
        ItemStack item = context.getIndexed(1, ItemStack.class, null);
        if (item == null)
        {
            paramNotFound(context, "core", "&cUnknown Item: &6%s&c!", context.getString(1));
        }
        if (!context.hasFlag("b") && BasicsPerm.COMMAND_GIVE_BLACKLIST.isAuthorized(context.getSender()))
        {
            if (this.basics.getConfiguration().blacklist.contains(item))
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
        context.sendMessage("basics", "&aYou gave &2%s &e%d %s&a!", user.getName(), amount, matname);
        user.sendMessage("basics", "&2%s &ajust gave you &e%d %s&a!", context.getSender().getName(), amount, matname);
    }

    @Command(
        names = { "item", "i" },
        desc = "Gives the specified Item to you",
        max = 2,
        min = 1,
        flags = { @Flag(longName = "blacklist", name = "b") },
        usage = "<material[:data]> [amount] [-blacklist]")
    public void item(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&eDid you try to use &6/give &eon your new I-Tem?");
        ItemStack item = context.getIndexed(0, ItemStack.class, null);
        if (item == null)
        {
            paramNotFound(context, "core", "&cUnknown Item: &6%s&c!", context.getString(0));
        }
        if (!context.hasFlag("b") && BasicsPerm.COMMAND_ITEM_BLACKLIST.isAuthorized(sender))
        {
            if (this.basics.getConfiguration().blacklist.contains(item))
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
                illegalParameter(context, "basics", "&cThe amount has to be a Number greater than 0!");
            }
        }
        item.setAmount(amount);
        sender.getInventory().addItem(item);
        sender.updateInventory();
        sender.sendMessage("basics", "&eReceived: %d %s ", amount, MaterialMatcher.get().getNameFor(item));
    }

    @Command(
        desc = "Refills the stack in hand",
        usage = "[-a]",
        flags = { @Flag( longName = "all", name = "a") },
        max = 0)
    public void more(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&cYou can't get enough of it. Don't you?");
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR)
        {
            invalidUsage(context, "basics", "&eMore nothing is still nothing!");
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
            sender.sendMessage("basics", "&aRefilled all stacks!");
        }
        else
        {
            sender.getItemInHand().setAmount(64);
            sender.sendMessage("basics", "&aRefilled stack in hand!");
        }
    }

    @Command(
        desc = "Repairs your items",
        flags = { @Flag(longName = "all", name = "a") },
        usage = "[-all]") // without item in hand
    public void repair(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&eIf you do this you'll loose your warranty!");
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
                sender.sendMessage("basics", "&eNo items to repair!");
            }
            else
            {
                sender.sendMessage("basics", "&aRepaired %d items!", repaired);
            }
        }
        else
        {
            ItemStack item = sender.getItemInHand();
            if (MaterialMatcher.get().isRepairable(item))
            {
                if (item.getDurability() == 0)
                {
                    sender.sendMessage("basics", "&eNo need to repair this!");
                    return;
                }
                item.setDurability((short)0);
                sender.sendMessage("basics", "&aItem repaired!");
            }
            else
            {
                sender.sendMessage("basics", "&eItem cannot be repaired!");
            }
        }
    }

    @Command(
        names = { "pt", "powertool" },
        desc = "Binds a command to the item in hand.",
        usage = "<command> [arguments]",
        flags = { @Flag(longName = "append", name = "add") })
    public void powertool(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eYou already have enough power!");
        if (sender.getItemInHand().getType().equals(Material.AIR))
        {
            blockCommand(context, "basics", "&eYou do not have an item in your hand to bound the command to!");
        }
        if (context.hasIndexed(0))
        {
            this.addPowerTool(sender, context.getStrings(0), context.hasFlag("add"));
        }
        else
        {
            CraftItemStack item = (CraftItemStack)sender.getItemInHand();
            NBTTagCompound tag = item.getHandle().getTag();
            if (tag != null)
            {
                tag.set("UniquePowerToolID", new NBTTagList());
            }
            context.sendMessage("basics", "&aRemoved all commands bound to this item!");
        }
    }

    private void addPowerTool(User user, String command, boolean add)
    {
        CraftItemStack item = (CraftItemStack)user.getItemInHand();
        NBTTagCompound tag = item.getHandle().getTag();
        if (tag == null)
        {
            item.getHandle().setTag(tag = new NBTTagCompound());
        }
        NBTTagList ptVals;
        if (add)
        {
            ptVals = (NBTTagList)tag.get("UniquePowerToolID");
            if (ptVals == null)
            {
                tag.set("UniquePowerToolID", ptVals = new NBTTagList());
            }
        }
        else
        {
            tag.set("UniquePowerToolID", ptVals = new NBTTagList());
        }
        StringBuilder sb = new StringBuilder(_(user, "basics", "command(s) bound to this item:"));
        int i = 0;
        for (; i < ptVals.size(); i++)
        {
            sb.append("\n&f").append(((NBTTagString)ptVals.get(i)).data);
        }
        ptVals.add(new NBTTagString(command, command));//what key should i take?
        user.sendMessage("basics", "&6%d &e%s\n&aNew: &e%s", i + 1, sb.toString(), command);
    }
}
