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
import java.util.Map.Entry;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.readers.IntegerOrAllReader;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.context.Flag;
import de.cubeisland.engine.core.command.reflected.context.Flags;
import de.cubeisland.engine.core.command.reflected.context.Grouped;
import de.cubeisland.engine.core.command.reflected.context.IParams;
import de.cubeisland.engine.core.command.reflected.context.Indexed;
import de.cubeisland.engine.core.command.reflected.context.NParams;
import de.cubeisland.engine.core.command.reflected.context.Named;
import de.cubeisland.engine.core.command.result.paginated.PaginatedResult;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.matcher.Match;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static org.bukkit.Material.AIR;

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
    private final Basics module;

    public ItemCommands(Basics module)
    {
        this.module = module;
    }

    @Command(desc = "Looks up an item for you!")
    @IParams(@Grouped(req = false, value = @Indexed(label = "item")))
    public PaginatedResult itemDB(CommandContext context)
    {
        if (context.hasArg(0))
        {
            TreeSet<Entry<ItemStack, Double>> itemSet = Match.material().itemStackList(context.<String>getArg(0));
            if (itemSet != null && itemSet.size() > 0)
            {
                List<String> lines = new ArrayList<>();

                lines.add(context.getSender().getTranslation(POSITIVE, "Best Matched {input#item} ({integer#id}:{short#data}) for {input}", Match.material().getNameFor(itemSet.first().getKey()), itemSet.first().getKey().getType().getId(), itemSet.first().getKey().getDurability(), context.getArg(0)));
                itemSet.remove(itemSet.first());
                for (Entry<ItemStack, Double> item : itemSet) {
                    lines.add(context.getSender().getTranslation(POSITIVE,
                                                                 "Matched {input#item} ({integer#id}:{short#data}) for {input}",
                                                                 Match.material().getNameFor(item.getKey()),
                                                                 item.getKey().getType().getId(),
                                                                 item.getKey().getDurability(), context.getArg(0)));
                }
                return new PaginatedResult(context, lines);
            }
            else
            {
                context.sendTranslated(NEGATIVE, "Could not find any item named {input}!", context.getArg(0));
            }
        }
        else if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (sender.getItemInHand().getType().equals(AIR))
            {
                context.sendTranslated(NEUTRAL, "You hold nothing in your hands!");
            }
            else
            {
                ItemStack item = sender.getItemInHand();
                String found = Match.material().getNameFor(item);
                if (found == null)
                {
                    context.sendTranslated(NEGATIVE, "Itemname unknown! Itemdata: {integer#id}:{short#data}", item.getType().getId(), item.getDurability());
                }
                else
                {
                    context.sendTranslated(POSITIVE, "The Item in your hand is: {input#item} ({integer#id}:{short#data})", found, item.getType().getId(), item.getDurability());
                }
            }
        }
        else
        {
            context.sendTranslated(NEGATIVE, "You need 1 parameter!");
        }
        return null;
    }

    @Command(desc = "Changes the display name of the item in your hand.")
    @IParams({@Grouped(@Indexed(label = "name")),
              @Grouped(req = false, value = @Indexed(label = "lore..."),greedy = true)})
    public void rename(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            ItemStack item = sender.getItemInHand();
            if (item == null || item.getType().equals(AIR))
            {
                context.sendTranslated(NEGATIVE, "You need to hold an item to rename in your hand!");
                return;
            }
            ItemMeta meta = item.getItemMeta();
            String name = ChatFormat.parseFormats(context.<String>getArg(0));
            meta.setDisplayName(name);
            ArrayList<String> list = new ArrayList<>();
            for (int i = 1; i < context.getArgCount(); ++i)
            {
                list.add(ChatFormat.parseFormats(context.<String>getArg(i)));
            }
            meta.setLore(list);
            item.setItemMeta(meta);
            context.sendTranslated(POSITIVE, "You now hold {input#name} in your hands!", name);
            return;
        }
        context.sendTranslated(NEGATIVE, "Trying to give your {text:toys} a name?");
    }

    @Command(alias = "skullchange", desc = "Changes a skull to a players skin.")
    @IParams(@Grouped(req = false, value = @Indexed(label = "name")))
    public void headchange(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            String name = context.getArg(0);
            if (sender.getItemInHand().getType().equals(Material.SKULL_ITEM))
            {
                sender.getItemInHand().setDurability((short)3);
                SkullMeta meta = ((SkullMeta)sender.getItemInHand().getItemMeta());
                meta.setOwner(name);
                sender.getItemInHand().setItemMeta(meta);
                context.sendTranslated(POSITIVE, "You now hold {user}'s head in your hands!", name);
                return;
            }
            context.sendTranslated(NEGATIVE, "You are not holding a head.");
            return;
        }
        context.sendTranslated(NEGATIVE, "This will you only give headaches!");
    }

    @Command(desc = "Grants unlimited items")
    @IParams(@Grouped(req = false, value = @Indexed(label = {"!on","!off"})))
    public void unlimited(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            boolean unlimited;
            if (context.hasArg(0))
            {
                if ("on".equalsIgnoreCase(context.<String>getArg(0)))
                {
                    unlimited = true;
                }
                else if ("off".equalsIgnoreCase(context.<String>getArg(0)))
                {
                    unlimited = false;
                }
                else
                {
                    context.sendTranslated(NEUTRAL, "Invalid parameter! Use {text:on} or {text:off}!");
                    return;
                }
            }
            else
            {
                unlimited = sender.get(BasicsAttachment.class).hasUnlimitedItems();
            }
            if (unlimited)
            {
                context.sendTranslated(POSITIVE, "You now have unlimited items to build!");
            }
            else
            {
                context.sendTranslated(NEUTRAL, "You no longer have unlimited items to build!");
            }
            sender.get(BasicsAttachment.class).setUnlimitedItems(unlimited);
            return;
        }
        context.sendTranslated(NEGATIVE, "This command can only be used by a player!");
    }

    @Command(desc = "Adds an Enchantment to the item in your hand")
    @IParams({@Grouped(value = @Indexed(label = "enchantment"), req = false),
              @Grouped(value = @Indexed(label = "level"), req = false)})
    @Flags(@Flag(longName = "unsafe", name = "u"))
    public void enchant(ParameterizedContext context)
    {
        if (!context.hasArg(0))
        {
            context.sendTranslated(POSITIVE, "Following Enchantments are availiable:\n{input#enchs}", this.getPossibleEnchantments(null));
            return;
        }
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            ItemStack item = sender.getItemInHand();
            if (item.getType().equals(AIR))
            {
                context.sendTranslated(NEUTRAL, "{text:ProTip}: You cannot enchant your fists!");
                return;
            }
            Enchantment ench = context.getArg(0, null);
            if (ench == null)
            {
                String possibleEnchs = this.getPossibleEnchantments(item);
                if (possibleEnchs != null)
                {
                    context.sendTranslated(NEGATIVE, "Enchantment {input#enchantment} not found!", context.getArg(0));
                    context.sendTranslated(NEUTRAL, "Try one of those instead:");
                    context.sendMessage(possibleEnchs);
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "You can not enchant this item!");
                }
                return;
            }
            int level = ench.getMaxLevel();
            if (context.hasArg(1))
            {
                level = context.getArg(1, 0);
                if (level <= 0)
                {
                    context.sendTranslated(NEGATIVE, "The enchantment level has to be a number greater than 0!");
                    return;
                }
            }
            if (context.hasFlag("u"))
            {
                if (module.perms().COMMAND_ENCHANT_UNSAFE.isAuthorized(sender))
                {
                    if (item.getItemMeta() instanceof EnchantmentStorageMeta)
                    {
                        EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta)item.getItemMeta();
                        itemMeta.addStoredEnchant(ench, level, true);
                        item.setItemMeta(itemMeta);
                        return;
                    }
                    // TODO enchant item event when bukkit event is not only for enchanting via table #WaitForBukkit
                    item.addUnsafeEnchantment(ench, level);
                    context.sendTranslated(POSITIVE, "Added unsafe enchantment: {input#enchantment} {integer#level} to your item!", Match.enchant().nameFor(ench), level);
                    return;
                }
                context.sendTranslated(NEGATIVE, "You are not allowed to add unsafe enchantments!");
                return;
            }
            if (ench.canEnchantItem(item))
            {
                if (level >= ench.getStartLevel() && level <= ench.getMaxLevel())
                {
                    item.addUnsafeEnchantment(ench, level);
                    context.sendTranslated(POSITIVE, "Added enchantment: {input#enchantment} {integer#level} to your item!", Match.enchant().nameFor(ench), level);
                    return;
                }
                context.sendTranslated(NEGATIVE, "This enchantment level is not allowed!");
                return;
            }
            String possibleEnchs = this.getPossibleEnchantments(item);
            if (possibleEnchs != null)
            {
                context.sendTranslated(NEGATIVE, "This enchantment is not allowed for this item!", possibleEnchs);
                context.sendTranslated(NEUTRAL, "Try one of those instead:");
                context.sendMessage(possibleEnchs);
                return;
            }
            context.sendTranslated(NEGATIVE, "You can not enchant this item!");
            return;
        }
        context.sendTranslated(NEUTRAL, "Want to be Harry Potter?");
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
                    sb.append(ChatFormat.YELLOW).append(Match.enchant().nameFor(enchantment));
                    first = false;
                }
                else
                {
                    sb.append(ChatFormat.WHITE).append(", ").append(ChatFormat.YELLOW).append(Match.enchant()
                                                                                                   .nameFor(enchantment));
                }
            }
        }
        if (sb.length() == 0)
        {
            return null;
       }
        return sb.toString();
    }

    @Command(desc = "Gives the specified Item to a player")
    @IParams({@Grouped(@Indexed(label = "player", type = User.class)),
              @Grouped(@Indexed(label = "material[:data]")),
              @Grouped(value = @Indexed(label = "amount"), req = false)})
    @Flags(@Flag(name = "b", longName = "blacklist"))
    @SuppressWarnings("deprecation")
    public void give(ParameterizedContext context)
    {
        User user = context.getArg(0);
        if (user == null)
        {
            context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getArg(0));
            return;
        }
        ItemStack item = context.getArg(1, null);
        if (item == null)
        {
            context.sendTranslated(NEGATIVE, "Unknown Item: {input#item}!", context.getArg(1));
            return;
        }
        if (!context.hasFlag("b") && module.perms().ITEM_BLACKLIST.isAuthorized(context.getSender())
            && this.module.getConfiguration().commands.itemBlacklist.contains(item))
        {
            context.sendTranslated(NEGATIVE, "This item is blacklisted!");
            return;
        }
        int amount = item.getMaxStackSize();
        if (context.hasArg(2))
        {
            amount = context.getArg(2, 0);
            if (amount == 0)
            {
                context.sendTranslated(NEGATIVE, "The amount has to be a number greater than 0!");
                return;
            }
        }
        item.setAmount(amount);
        user.getInventory().addItem(item);
        user.updateInventory();
        String matname = Match.material().getNameFor(item);
        context.sendTranslated(POSITIVE, "You gave {user} {amount} {input#item}!", user, amount, matname);
        user.sendTranslated(POSITIVE, "{user} just gave you {amount} {input#item}!", context.getSender().getName(), amount, matname);
    }

    @Command(alias = "i", desc = "Gives the specified Item to you")
    @IParams({@Grouped(@Indexed(label = "material[:data]", type = ItemStack.class)),
              @Grouped(value = @Indexed(label = "amount", type = Integer.class), req = false)})
    @NParams(@Named(names = "ench", label = "enchantment[:level]"))
    @Flags(@Flag(longName = "blacklist", name = "b"))
    @SuppressWarnings("deprecation")
    public void item(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            ItemStack item = context.getArg(0, null);
            if (!context.hasFlag("b") && module.perms().ITEM_BLACKLIST.isAuthorized(sender)
                    && this.module.getConfiguration().commands.containsBlackListed(item))
            {
                context.sendTranslated(NEGATIVE, "This item is blacklisted!");
                return;
            }
            int amount = item.getMaxStackSize();
            if (context.hasArg(0))
            {
                amount = context.getArg(1, 0);
                if (amount <= 0)
                {
                    context.sendTranslated(NEGATIVE, "The amount has to be a Number greater than 0!");
                    return;
                }
            }

            if (context.hasParam("ench"))
            {
                String[] enchs = StringUtils.explode(",", context.getString("ench"));
                for (String ench : enchs)
                {
                    int enchLvl = 0;
                    if (ench.contains(":"))
                    {
                        enchLvl = Integer.parseInt(ench.substring(ench.indexOf(":") + 1, ench.length()));
                        ench = ench.substring(0, ench.indexOf(":"));
                    }
                    if (module.perms().COMMAND_ITEM_ENCHANTMENTS.isAuthorized(sender))
                    {
                        if (module.perms().COMMAND_ITEM_ENCHANTMENTS_UNSAFE.isAuthorized(sender))
                        {
                            Match.enchant().applyMatchedEnchantment(item, ench, enchLvl, true);
                        }
                        else
                        {
                            Match.enchant().applyMatchedEnchantment(item, ench, enchLvl, false);
                        }
                    }
                }
            }
            item.setAmount(amount);
            sender.getInventory().addItem(item);
            sender.updateInventory();
            sender.sendTranslated(NEUTRAL, "Received: {amount} {input#item}", amount, Match.material().getNameFor(item));
            return;
        }
        context.sendTranslated(NEUTRAL, "Did you try to use {text:/give} on your new I-Tem?");
    }

    @Command(desc = "Refills the stack in hand")
    @IParams(@Grouped(value = @Indexed(label = {"amount","!*"}, type = IntegerOrAllReader.class), req = false))
    public void more(CommandContext context)
    {
        if (!context.isSender(User.class))
        {
            context.sendTranslated(NEGATIVE, "You can't get enough of it, can you?");
            return;
        }
        User sender = (User)context.getSender();
        Integer amount = 1;
        if (context.hasArg(0))
        {
            if ("*".equals(context.getArg(0)))
            {
                for (ItemStack item : sender.getInventory().getContents())
                {
                    if (item.getType() != AIR)
                    {
                        item.setAmount(64);
                    }
                }
                sender.sendTranslated(POSITIVE, "Refilled all stacks!");
                return;
            }
            amount = context.getArg(0);
            if (amount <= 1)
            {
                context.sendTranslated(NEGATIVE, "Invalid amount {input#amount}", amount);
                return;
            }
        }
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == AIR)
        {
            context.sendTranslated(NEUTRAL, "More nothing is still nothing!");
            return;
        }
        sender.getItemInHand().setAmount(64);
        if (amount == 1)
        {
            sender.sendTranslated(POSITIVE, "Refilled stack in hand!");
            return;
        }
        for (int i = 1; i < amount; ++i)
        {
            sender.getInventory().addItem(sender.getItemInHand());
        }
        sender.sendTranslated(POSITIVE, "Refilled {amount} stacks in hand!", amount);
    }

    @Command(desc = "Repairs your items")
    @Flags(@Flag(longName = "all", name = "a"))
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
                    sender.sendTranslated(NEUTRAL, "No items to repair!");
                    return;
                }
                sender.sendTranslated(POSITIVE, "Repaired {amount} items!", repaired);
                return;
            }
            ItemStack item = sender.getItemInHand();
            if (Match.material().repairable(item))
            {
                if (item.getDurability() == 0)
                {
                    sender.sendTranslated(NEUTRAL, "No need to repair this!");
                    return;
                }
                item.setDurability((short)0);
                sender.sendTranslated(POSITIVE, "Item repaired!");
                return;
            }
            sender.sendTranslated(NEUTRAL, "Item cannot be repaired!");
            return;
        }
        context.sendTranslated(NEGATIVE, "If you do this you'll loose your warranty!");
    }

    @Command(desc = "Stacks your items up to 64")
    public void stack(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User user = (User)context.getSender();
            boolean allow64 = module.perms().COMMAND_STACK_FULLSTACK.isAuthorized(user);
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
                user.sendTranslated(POSITIVE, "Items stacked together!");
                return;
            }
            user.sendTranslated(NEUTRAL, "Nothing to stack!");
            return;
        }
        context.sendTranslated(NEUTRAL, "No stacking for you.");
    }
}
