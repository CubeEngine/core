package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;

/**
 * item-related commands /itemdb /rename /headchange /unlimited /enchant /give
 * /item /more /repair /stack
 */
public class ItemCommands
{
    private Basics basics;

    public ItemCommands(Basics basics)
    {
        this.basics = basics;
    }

    @Command(desc = "Looks up an item for you!", max = 1, usage = "[item]")
    public void itemDB(CommandContext context)
    {
        if (context.hasArg(0))
        {
            ItemStack item = Match.material().itemStack(context.getString(0));
            if (item != null)
            {
                context.sendMessage("basics", "&aMatched &e%s &f(&e%d&f:&e%d&f) &afor &f%s",
                        Match.material().getNameFor(item), item.getType().getId(), item.getDurability(), context.getString(0));
            }
            else
            {
                context.sendMessage("basics", "&cCould not find any item named &e%s&c!", context.getString(0));
            }
            return;
        }
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (sender == null)
            {

                return;
            }
            if (sender.getItemInHand().getType().equals(Material.AIR))
            {
                invalidUsage(context, "basics", "&eYou hold nothing in your hands!");
            }
            else
            {
                ItemStack item = sender.getItemInHand();
                String found = Match.material().getNameFor(item);
                if (found == null)
                {
                    context.sendMessage("basics", "&cItemname unknown! Itemdata: &e%d&f:&e%d&f",
                            item.getType().getId(), item.getDurability());
                    return;
                }
                context.sendMessage("basics", "&aThe Item in your hand is: &e%s &f(&e%d&f:&e%d&f)",
                        found, item.getType().getId(), item.getDurability());
            }
            return;
        }
        context.sendMessage("basics", "&cYou need 1 parameter!");
    }

    @Command(desc = "Changes the display name of the item in your hand.", usage = "<name> [lore...]", min = 1)
    public void rename(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            ItemStack item = sender.getItemInHand();
            ItemMeta meta = item.getItemMeta();
            String name = ChatFormat.parseFormats(context.getString(0));
            meta.setDisplayName(name);
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 1; i < context.getArgCount(); ++i)
            {
                list.add(ChatFormat.parseFormats(context.getString(i)));
            }
            meta.setLore(list);
            item.setItemMeta(meta);
            context.sendMessage("basics", "&aYou now hold &6%s &ain your hands!", name);
            return;
        }
        context.sendMessage("basics", "&cTrying to give your &6toys &ca name?");
    }

    @Command(names = {
        "headchange", "skullchange"
    }, desc = "Changes a skull to a players skin.", usage = "<name>", min = 1)
    @SuppressWarnings("deprecation")
    public void headchange(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            String name = context.getString(0);
            if (sender.getItemInHand().getType().equals(Material.SKULL_ITEM))
            {
                sender.getItemInHand().setDurability((short)3);
                SkullMeta meta = ((SkullMeta)sender.getItemInHand().getItemMeta());
                meta.setOwner(name);
                sender.getItemInHand().setItemMeta(meta);
                context.sendMessage("basics", "&aYou now hold &6%s's &ahead in your hands!", name);
                return;
            }
            context.sendMessage("basics", "&cYou are not holding a head.");
            return;
        }
        context.sendMessage("basics", "&cThis will you only give headaches!");
    }

    @Command(desc = "The user can use unlimited items", max = 1, usage = "[on|off]")
    @SuppressWarnings("deprecation")
    public void unlimited(CommandContext context)
    {

        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            boolean unlimited = false;
            if (context.hasArg(0))
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
                    context.sendMessage("basics", "&eInvalid parameter! Use &aon &eor %coff&e!");
                    return;
                }
            }
            else
            {
                Object bln = sender.getAttribute(basics, "unlimitedItems");
                unlimited = bln == null;
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
            return;
        }
        context.sendMessage("core", "&cThis command can only be used by a player!");
    }

    @Command(desc = "Adds an Enchantment to the item in your hand", max = 2, flags = @Flag(longName = "unsafe", name = "u"), usage = "<enchantment> [level] [-unsafe]")
    public void enchant(ParameterizedContext context)
    {
        if (!context.hasArg(0))
        {
            context.sendMessage("&aFollowing Enchantments are availiable:\n%s", this.getPossibleEnchantments(null));
            return;
        }
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            ItemStack item = sender.getItemInHand();
            if (item.getType().equals(Material.AIR))
            {
                context.sendMessage("basics", "&6ProTip: &eYou cannot enchant your fists!");
                return;
            }
            Enchantment ench = context.getArg(0, Enchantment.class, null);
            if (ench == null)
            {
                String possibleEnchs = this.getPossibleEnchantments(item);
                if (possibleEnchs != null)
                {
                    context.sendMessage("basics", "&cEnchantment &6%s &cnot found! Try one of those instead:\n%s", context.
                            getString(0), possibleEnchs);
                }
                else
                {
                    context.sendMessage("basics", "&cYou can not enchant this item!");
                }
                return;
            }
            int level = ench.getMaxLevel();
            if (context.hasArg(1))
            {
                level = context.getArg(1, Integer.class, 0);
                if (level <= 0)
                {
                    context.sendMessage("basics", "&cThe enchantment-level has to be a number greater than 0!");
                    return;
                }
            }
            if (context.hasFlag("u"))
            {
                if (BasicsPerm.COMMAND_ENCHANT_UNSAFE.isAuthorized(sender))
                {
                    item.addUnsafeEnchantment(ench, level);
                    context.sendMessage("basics", "&aAdded unsafe enchantment: &6%s %d &ato your item!",
                            Match.enchant().nameFor(ench), level);
                    return;
                }
                context.sendMessage("basics", "&cYou are not allowed to add unsafe enchantments!");
                return;
            }
            if (ench.canEnchantItem(item))
            {
                if (level >= ench.getStartLevel() && level <= ench.getMaxLevel())
                {
                    item.addUnsafeEnchantment(ench, level);
                    context.sendMessage("bascics", "&aAdded enchantment: &6%s %d &ato your item!", Match.enchant().nameFor(ench), level);
                    return;
                }
                context.sendMessage("basics", "&cThis enchantment-level is not allowed!");
                return;
            }
            String possibleEnchs = this.getPossibleEnchantments(item);
            if (possibleEnchs != null)
            {
                context.sendMessage("basics", "&cThis enchantment is not allowed for this item!\n&eTry one of those instead:\n%s", possibleEnchs);
                return;
            }
            context.sendMessage("basics", "&cYou can not enchant this item!");
            return;
        }
        context.sendMessage("basics", "&eWant to be Harry Potter?");
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
                    sb.append("&e").append(Match.enchant().nameFor(enchantment));
                    first = false;
                }
                else
                {
                    sb.append("&f, &e").append(Match.enchant().nameFor(enchantment));
                }
            }
        }
        if (sb.length() == 0)
        {
            return null;
        }
        return sb.toString();
    }

    @Command(desc = "Gives the specified Item to a player", flags = {
        @Flag(name = "b", longName = "blacklist")
    }, min = 2, max = 3, usage = "<player> <material[:data]> [amount] [-blacklist]")
    @SuppressWarnings("deprecation")
    public void give(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendMessage("core", "&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        ItemStack item = context.getArg(1, ItemStack.class, null);
        if (item == null)
        {
            context.sendMessage("core", "&cUnknown Item: &6%s&c!", context.getString(1));
            return;
        }
        if (!context.hasFlag("b") && BasicsPerm.COMMAND_GIVE_BLACKLIST.isAuthorized(context.getSender())
            && this.basics.getConfiguration().blacklist.contains(item))
        {
            context.sendMessage("basics", "&cThis item is blacklisted!");
            return;
        }
        int amount = item.getMaxStackSize();
        if (context.hasArg(2))
        {
            amount = context.getArg(2, Integer.class, 0);
            if (amount == 0)
            {
                context.sendMessage("basics", "&cThe amount has to be a number greater than 0!");
                return;
            }
        }
        item.setAmount(amount);
        user.getInventory().addItem(item);
        user.updateInventory();
        String matname = Match.material().getNameFor(item);
        context.sendMessage("basics", "&aYou gave &2%s &e%d %s&a!", user.getName(), amount, matname);
        user.sendMessage("basics", "&2%s &ajust gave you &e%d %s&a!", context.getSender().getName(), amount, matname);
    }

    @Command(names = {
        "item", "i"
    }, desc = "Gives the specified Item to you", min = 1, flags = {
        @Flag(longName = "blacklist", name = "b")
    }, usage = "<material[:data]> [enchantment[:level]] [amount] [-blacklist]")
    @SuppressWarnings("deprecation")
    public void item(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            ItemStack item = context.getArg(0, ItemStack.class, null);
            if (item == null)
            {
                context.sendMessage("core", "&cUnknown Item: &6%s&c!", context.getString(0));
                return;
            }
            if (!context.hasFlag("b") && BasicsPerm.COMMAND_ITEM_BLACKLIST.isAuthorized(sender)
                    && this.basics.getConfiguration().blacklist.contains(item))
            {
                context.sendMessage("basics", "&cThis item is blacklisted!");
                return;
            }
            int amount = item.getMaxStackSize();
            int curIndex = 1;
            while (context.hasArg(curIndex))
            {
                String enchName = context.getString(curIndex);
                if (!enchName.matches("(?!^\\d+$)^.+$"))
                {
                    amount = context.getArg(curIndex, Integer.class, 0);
                    if (amount == 0)
                    {
                        context.sendMessage("basics", "&cThe amount has to be a Number greater than 0!");
                        return;
                    }
                    break;
                }
                int enchLvl = 0;
                if (enchName.contains(":"))
                {
                    enchLvl = Integer.parseInt(enchName.substring(enchName.indexOf(":") + 1, enchName.length()));
                    enchName = enchName.substring(0, enchName.indexOf(":"));
                }
                if (BasicsPerm.COMMAND_ITEM_ENCHANTMENTS.isAuthorized(sender))
                {
                    if (BasicsPerm.COMMAND_ITEM_ENCHANTMENTS_UNSAFE.isAuthorized(sender))
                    {
                        Match.enchant().applyMatchedEnchantment(item, enchName, enchLvl, true);
                    }
                    else
                    {
                        Match.enchant().applyMatchedEnchantment(item, enchName, enchLvl, false);
                    }
                }
                curIndex++;
            }
            item.setAmount(amount);
            sender.getInventory().addItem(item);
            sender.updateInventory();
            sender.sendMessage("basics", "&eReceived: %d %s ", amount, Match.material().getNameFor(item));
            return;
        }
        context.sendMessage("basics", "&eDid you try to use &6/give &eon your new I-Tem?");
    }

    @Command(desc = "Refills the stack in hand", usage = "[amount] [-a]", max = 1, flags = @Flag(longName = "all", name = "a"))
    public void more(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendMessage("basics", "&cYou can't get enough of it. Don't you?");
            return;
        }
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR)
        {
            context.sendMessage("basics", "&eMore nothing is still nothing!");
            return;
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
            if (context.hasArg(0))
            {
                Integer amount = context.getArg(0, Integer.class);
                if (amount == null || amount <= 1)
                {
                    context.sendMessage("basics", "&cInvalid amount! (%s)", context.getString(0));
                    return;
                }
                for (int i = 1; i < amount; ++i)
                {
                    sender.getInventory().addItem(sender.getItemInHand());
                }
                sender.sendMessage("basics", "&aRefilled &6%s &astacks in hand!", context.getString(0));
                return;
            }
            sender.sendMessage("basics", "&aRefilled stack in hand!");
        }
    }

    @Command(desc = "Repairs your items", flags = {
        @Flag(longName = "all", name = "a")
    }, usage = "[-all]")
    // without item in hand
    public void repair(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (context.hasFlag("a"))
            {
                List<ItemStack> list = new ArrayList<ItemStack>();
                list.addAll(Arrays.asList(sender.getInventory().getArmorContents()));
                list.addAll(Arrays.asList(sender.getInventory().getContents()));
                int repaired = 0;
                for (ItemStack item : list)
                {
                    if (Match.material().repairable(item))
                    {
                        item.setDurability((short)0);
                        repaired++;
                    }
                }
                if (repaired == 0)
                {
                    sender.sendMessage("basics", "&eNo items to repair!");
                    return;
                }
                sender.sendMessage("basics", "&aRepaired %d items!", repaired);
                return;
            }
            ItemStack item = sender.getItemInHand();
            if (Match.material().repairable(item))
            {
                if (item.getDurability() == 0)
                {
                    sender.sendMessage("basics", "&eNo need to repair this!");
                    return;
                }
                item.setDurability((short)0);
                sender.sendMessage("basics", "&aItem repaired!");
                return;
            }
            sender.sendMessage("basics", "&eItem cannot be repaired!");
            return;
        }
        context.sendMessage("core", "&eIf you do this you'll &cloose &eyour warranty!");
    }

    @Command(desc = "Stacks your items up to 64")
    public void stack(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User user = (User)context.getSender();
            boolean allow64 = BasicsPerm.COMMAND_STACK_FULLSTACK.isAuthorized(user);
            ItemStack[] items = user.getInventory().getContents();
            int size = items.length;
            boolean changed = false;
            for (int i = 0; i < size; i++)
            {
                ItemStack item = items[i];
                // no null / infinite or unstackable items (if not allowed)
                if (item == null || item.getAmount() <= 0 || (!allow64 && item.getMaxStackSize() == 1))
                {
                    continue;
                }
                int max = allow64 ? 64 : item.getMaxStackSize();
                if (item.getAmount() < max)
                {
                    int needed = max - item.getAmount();
                    for (int j = i + 1; j < size; j++) // search for same item
                    {
                        ItemStack item2 = items[j];
                        // no null / infinite or unstackable items (if not allowed)
                        if (item2 == null || item2.getAmount() <= 0 || (!allow64 && item.getMaxStackSize() == 1))
                        {
                            continue;
                        }
                        // compare
                        if (item.isSimilar(item2))
                        {
                            if (item2.getAmount() > needed) // not enough place -> fill up stack
                            {
                                item.setAmount(max);
                                item2.setAmount(item2.getAmount() - needed);
                                break;
                            }
                            // enough place -> add to stack
                            {
                                items[j] = null;
                                item.setAmount(item.getAmount() + item2.getAmount());
                                needed = max - item.getAmount();
                            }
                            changed = true;
                        }
                    }
                }
            }
            if (changed)
            {
                user.getInventory().setContents(items);
                user.sendMessage("&aItems stacked together!");
                return;
            }
            user.sendMessage("&eNothing to stack!");
            return;
        }
        context.sendMessage("basics", "&eNo stacking for you.");
    }
}
