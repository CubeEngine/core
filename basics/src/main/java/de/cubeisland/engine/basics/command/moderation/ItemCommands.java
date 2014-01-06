/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.basics.command.moderation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.basics.BasicsPerm;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.matcher.Match;

import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;

/**
 * item-related commands
 * <p>/itemdb
 * <p>/rename
 * <p>/headchange
 * <p>/unlimited
 * <p>/enchant
 * <p>/give
 * <p>/item
 * <p>/more
 * <p>/repair
 * <p>/stack
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
                context.sendTranslated("&aMatched &e%s &f(&e%d&f:&e%d&f) &afor &f%s",
                                       Match.material().getNameFor(item), item.getType().getId(), item.getDurability(), context.getString(0));
            }
            else
            {
                context.sendTranslated("&cCould not find any item named &e%s&c!", context.getString(0));
            }
            return;
        }
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (sender.getItemInHand().getType().equals(Material.AIR))
            {
                context.sendTranslated("&eYou hold nothing in your hands!");
                return;
            }
            else
            {
                ItemStack item = sender.getItemInHand();
                String found = Match.material().getNameFor(item);
                if (found == null)
                {
                    context.sendTranslated("&cItemname unknown! Itemdata: &e%d&f:&e%d&f",
                                           item.getType().getId(), item.getDurability());
                    return;
                }
                context.sendTranslated("&aThe Item in your hand is: &e%s &f(&e%d&f:&e%d&f)",
                                       found, item.getType().getId(), item.getDurability());
            }
            return;
        }
        context.sendTranslated("&cYou need 1 parameter!");
    }

    @Command(desc = "Changes the display name of the item in your hand.", usage = "<name> [lore...]", min = 1, max = NO_MAX)
    public void rename(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            ItemStack item = sender.getItemInHand();
            if (item == null || item.getType().equals(Material.AIR))
            {
                context.sendTranslated("&cYou need to hold an item to rename in your hand!");
                return;
            }
            ItemMeta meta = item.getItemMeta();
            String name = ChatFormat.parseFormats(context.getString(0));
            meta.setDisplayName(name);
            ArrayList<String> list = new ArrayList<>();
            for (int i = 1; i < context.getArgCount(); ++i)
            {
                list.add(ChatFormat.parseFormats(context.getString(i)));
            }
            meta.setLore(list);
            item.setItemMeta(meta);
            context.sendTranslated("&aYou now hold &6%s&a in your hands!", name);
            return;
        }
        context.sendTranslated("&cTrying to give your &6toys &ca name?");
    }

    @Command(names = {
        "headchange", "skullchange"
    }, desc = "Changes a skull to a players skin.", usage = "<name>", min = 1, max = 1)
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
                context.sendTranslated("&aYou now hold &6%s's &ahead in your hands!", name);
                return;
            }
            context.sendTranslated("&cYou are not holding a head.");
            return;
        }
        context.sendTranslated("&cThis will you only give headaches!");
    }

    @Command(desc = "The user can use unlimited items", max = 1, usage = "[on|off]")
    @SuppressWarnings("deprecation")
    public void unlimited(CommandContext context)
    {

        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            boolean unlimited;
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
                    context.sendTranslated("&eInvalid parameter! Use &aon &eor %coff&e!");
                    return;
                }
            }
            else
            {
                unlimited = sender.get(BasicsAttachment.class).hasUnlimitedItems();
            }
            if (unlimited)
            {
                sender.get(BasicsAttachment.class).setUnlimitedItems(unlimited);
                context.sendTranslated("&aYou now have unlimited items to build!");
            }
            else
            {
                sender.get(BasicsAttachment.class).setUnlimitedItems(unlimited);
                context.sendTranslated("&eYou now no longer have unlimited items to build!");
            }
            return;
        }
        context.sendTranslated("&cThis command can only be used by a player!");
    }

    @Command(desc = "Adds an Enchantment to the item in your hand", max = 2,
             flags = @Flag(longName = "unsafe", name = "u"),
             usage = "<enchantment> [level] [-unsafe]")
    public void enchant(ParameterizedContext context)
    {
        if (!context.hasArg(0))
        {
            context.sendTranslated("&aFollowing Enchantments are availiable:\n%s", this.getPossibleEnchantments(null));
            return;
        }
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            ItemStack item = sender.getItemInHand();
            if (item.getType().equals(Material.AIR))
            {
                context.sendTranslated("&6ProTip: &eYou cannot enchant your fists!");
                return;
            }
            Enchantment ench = context.getArg(0, Enchantment.class, null);
            if (ench == null)
            {
                String possibleEnchs = this.getPossibleEnchantments(item);
                if (possibleEnchs != null)
                {
                    context.sendTranslated("&cEnchantment &6%s &cnot found! Try one of those instead:\n%s", context.getString(0), possibleEnchs);
                }
                else
                {
                    context.sendTranslated("&cYou can not enchant this item!");
                }
                return;
            }
            int level = ench.getMaxLevel();
            if (context.hasArg(1))
            {
                level = context.getArg(1, Integer.class, 0);
                if (level <= 0)
                {
                    context.sendTranslated("&cThe enchantment-level has to be a number greater than 0!");
                    return;
                }
            }
            if (context.hasFlag("u"))
            {
                if (BasicsPerm.COMMAND_ENCHANT_UNSAFE.isAuthorized(sender))
                {
                    if (item.getItemMeta() instanceof EnchantmentStorageMeta)
                    {
                        EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta)item.getItemMeta();
                        itemMeta.addStoredEnchant(ench, level, true);
                        item.setItemMeta(itemMeta);
                        return;
                    }
                    // TODO enchant item event ?
                    item.addUnsafeEnchantment(ench, level);
                    context.sendTranslated("&aAdded unsafe enchantment: &6%s %d &ato your item!",
                                           Match.enchant().nameFor(ench), level);
                    return;
                }
                context.sendTranslated("&cYou are not allowed to add unsafe enchantments!");
                return;
            }
            if (ench.canEnchantItem(item))
            {
                if (level >= ench.getStartLevel() && level <= ench.getMaxLevel())
                {
                    item.addUnsafeEnchantment(ench, level);
                    context.sendTranslated("&aAdded enchantment: &6%s %d &ato your item!", Match.enchant().nameFor(ench), level);
                    return;
                }
                context.sendTranslated("&cThis enchantment-level is not allowed!");
                return;
            }
            String possibleEnchs = this.getPossibleEnchantments(item);
            if (possibleEnchs != null)
            {
                context.sendTranslated("&cThis enchantment is not allowed for this item!\n&eTry one of those instead:\n%s", possibleEnchs);
                return;
            }
            context.sendTranslated("&cYou can not enchant this item!");
            return;
        }
        context.sendTranslated("&eWant to be Harry Potter?");
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
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        ItemStack item = context.getArg(1, ItemStack.class, null);
        if (item == null)
        {
            context.sendTranslated("&cUnknown Item: &6%s&c!", context.getString(1));
            return;
        }
        if (!context.hasFlag("b") && BasicsPerm.ITEM_BLACKLIST.isAuthorized(context.getSender())
            && this.basics.getConfiguration().commands.itemBlacklist.contains(item))
        {
            context.sendTranslated("&cThis item is blacklisted!");
            return;
        }
        int amount = item.getMaxStackSize();
        if (context.hasArg(2))
        {
            amount = context.getArg(2, Integer.class, 0);
            if (amount == 0)
            {
                context.sendTranslated("&cThe amount has to be a number greater than 0!");
                return;
            }
        }
        item.setAmount(amount);
        user.getInventory().addItem(item);
        user.updateInventory();
        String matname = Match.material().getNameFor(item);
        context.sendTranslated("&aYou gave &2%s &e%d %s&a!", user.getName(), amount, matname);
        user.sendTranslated("&2%s &ajust gave you &e%d %s&a!", context.getSender().getName(), amount, matname);
    }

    @Command(names = {
        "item", "i"
    }, desc = "Gives the specified Item to you", min = 1, max = NO_MAX, flags = {
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
                context.sendTranslated("&cUnknown Item: &6%s&c!", context.getString(0));
                return;
            }
            if (!context.hasFlag("b") && BasicsPerm.ITEM_BLACKLIST.isAuthorized(sender)
                    && this.basics.getConfiguration().commands.containsBlackListed(item))
            {
                context.sendTranslated("&cThis item is blacklisted!");
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
                        context.sendTranslated("&cThe amount has to be a Number greater than 0!");
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
            sender.sendTranslated("&eReceived: %d %s ", amount, Match.material().getNameFor(item));
            return;
        }
        context.sendTranslated("&eDid you try to use &6/give &eon your new I-Tem?");
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
            context.sendTranslated("&cYou can't get enough of it. Don't you?");
            return;
        }
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR)
        {
            context.sendTranslated("&eMore nothing is still nothing!");
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
            sender.sendTranslated("&aRefilled all stacks!");
        }
        else
        {
            sender.getItemInHand().setAmount(64);
            if (context.hasArg(0))
            {
                Integer amount = context.getArg(0, Integer.class);
                if (amount == null || amount <= 1)
                {
                    context.sendTranslated("&cInvalid amount! (%s)", context.getString(0));
                    return;
                }
                for (int i = 1; i < amount; ++i)
                {
                    sender.getInventory().addItem(sender.getItemInHand());
                }
                sender.sendTranslated("&aRefilled &6%s &astacks in hand!", context.getString(0));
                return;
            }
            sender.sendTranslated("&aRefilled stack in hand!");
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
                List<ItemStack> list = new ArrayList<>();
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
                    sender.sendTranslated("&eNo items to repair!");
                    return;
                }
                sender.sendTranslated("&aRepaired %d items!", repaired);
                return;
            }
            ItemStack item = sender.getItemInHand();
            if (Match.material().repairable(item))
            {
                if (item.getDurability() == 0)
                {
                    sender.sendTranslated("&eNo need to repair this!");
                    return;
                }
                item.setDurability((short)0);
                sender.sendTranslated("&aItem repaired!");
                return;
            }
            sender.sendTranslated("&eItem cannot be repaired!");
            return;
        }
        context.sendTranslated("&eIf you do this you'll &cloose &eyour warranty!");
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
        context.sendTranslated("&eNo stacking for you.");
    }
}
