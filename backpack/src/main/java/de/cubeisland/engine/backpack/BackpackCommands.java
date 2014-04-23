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
package de.cubeisland.engine.backpack;

import java.util.HashSet;

import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.parameterized.completer.WorldCompleter;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.matcher.Match;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static java.util.Arrays.asList;

public class BackpackCommands extends ContainerCommand
{
    private final Backpack module;
    private final BackpackManager manager;

    public BackpackCommands(Backpack module, BackpackManager manager)
    {
        super(module, "backpack", "The Backpack commands");
        this.module = module;
        this.setAliases(new HashSet<>(asList("bp")));
        this.manager = manager;
    }

    @Alias(names = "openbp")
    @Command(desc = "opens a backpack",
             indexed = {@Grouped(@Indexed("name")),
                        @Grouped(req = false, value = @Indexed("user"))},
             params = @Param(names = {"world", "for", "in", "w"}, completer = WorldCompleter.class, type = World.class))
    public void open(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User forUser = (User)context.getSender();
            if (context.hasArg(1))
            {
                forUser = context.getUser(1);
                if (forUser == null)
                {
                    context.sendTranslated(NEGATIVE, "User {user} not found!", context.getString(1));
                    return;
                }
            }
            World forWorld = forUser.getWorld();
            if (context.hasParam("w"))
            {
                forWorld = context.getParam("w", null);
                if (forWorld == null)
                {
                    context.sendTranslated(NEGATIVE, "Unknown World {input#world}!", context.getString("w"));
                    return;
                }
            }
            if (context.getSender() != forUser && !module.perms().OPEN_OTHER_USER.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to open the backpacks of other users!");
                return;
            }
            if (forUser.getWorld() != forWorld && ! module.perms().OPEN_OTHER_WORLDS.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to open backpacks from an other world!");
                return;
            }
            manager.openBackpack((User)context.getSender(), forUser, forWorld, context.getString(0));
            return;
        }
        context.sendTranslated(NEGATIVE, "You cannot open a inventory in console!"); // TODO perhaps save inventory to yml
    }

    @Alias(names = "createbp")
    @Command(desc = "creates a new backpack",
             indexed = {@Grouped(@Indexed("name")),
                        @Grouped(req = false, value = @Indexed("user"))},
             flags = {@Flag(name = "g", longName = "global"), // TODO OR flags
                      @Flag(name = "s", longName = "single"),
                      @Flag(name = "b", longName = "blockinput")},
             params = {@Param(names = {"w", "world", "for", "in"}, completer = WorldCompleter.class, type = World.class),
                       @Param(names = {"p", "pages"}, type = Integer.class),
                       @Param(names = {"s","size"}, type = Integer.class)})
    public void create(ParameterizedContext context)
    {
        User forUser = null;
        World forWorld = null;
        if (context.hasParam("w"))
        {
            forWorld = context.getParam("w", null);
            if (forWorld == null)
            {
                context.sendTranslated(NEGATIVE, "Unknown World {input#world}!", context.getString("w"));
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            forUser = (User)context.getSender();
            forWorld = ((User)context.getSender()).getWorld();
        }
        else if (!context.hasFlag("g"))
        {
            context.sendTranslated(POSITIVE, "You have to specify a world for non global backpacks!");
            return;
        }
        if (context.hasArg(1))
        {
            forUser = context.getUser(1);
            if (forUser == null)
            {
                context.sendTranslated(NEGATIVE, "User {user} not found!", context.getString(1));
                return;
            }
        }
        else if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(NEGATIVE, "You need to specify a user");
            return;
        }
        manager.createBackpack(context.getSender(), forUser, context.getString(0), forWorld,
                               context.hasFlag("g"), context.hasFlag("s"), context.hasFlag("b"),
                               context.getParam("p", 1), context.getParam("s", 6));
    }

    @Alias(names = "modifybp")
    @Command(desc = "modifies a backpack",
             indexed = {@Grouped(@Indexed("name")),
                        @Grouped(req = false, value = @Indexed("user"))},
             params = {@Param(names = {"pages","p"}, type = Integer.class),
                       @Param(names = {"size","s"}, type = Integer.class),
                       @Param(names = {"blockinput","b"}, type = Boolean.class, label = "true|false"),
                       @Param(names = {"world", "for", "in", "w"}, completer = WorldCompleter.class, type = World.class)})
    public void modify(ParameterizedContext context)
    {
        User forUser = null;
        World forWorld = null;
        if (context.getSender() instanceof User)
        {
            forUser = (User)context.getSender();
            forWorld = ((User)context.getSender()).getWorld();
        }
        else if (context.hasParam("w"))
        {
            forWorld = context.getParam("w", null);
            if (forWorld == null)
            {
                context.sendTranslated(NEGATIVE, "Unknown World {input#world}!", context.getString("w"));
                return;
            }
        }
        else if (!context.hasFlag("g"))
        {
            context.sendTranslated(POSITIVE, "You have to specify a world for non global backpacks!");
            return;
        }
        if (context.hasArg(1))
        {
            forUser = context.getUser(1);
            if (forUser == null)
            {
                context.sendTranslated(NEGATIVE, "User {user} not found!", context.getString(1));
                return;
            }
        }
        else if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(NEGATIVE, "You need to specify a user");
            return;
        }
        manager.modifyBackpack(context.getSender(), forUser, context.getString(0), forWorld,
                               (Integer)context.getParam("p", null),
                               (Boolean)context.getParam("b", null),
                               (Integer)context.getParam("s", null));
    }

    @Alias(names = "givebp")
    @Command(desc = "Puts items into a backpack",
             indexed = {@Grouped(@Indexed("name")),
                        @Grouped(req = false, value = @Indexed("user"))},
    params = {@Param(names = {"item","i"}, required = true, label = "item[:data]"),
              @Param(names = {"name","n"}),
              @Param(names = {"lore","l"}, label = "lorelines..."),
              @Param(names = {"amount","a"}, type = Integer.class),
              @Param(names = {"ench", "enchantments","e"}, label = "enchs..."),
              @Param(names = {"world", "for", "in", "w"},
                     completer = WorldCompleter.class, type = World.class)})
    // /givebp premium Faithcaio item diamondpick:1500 name "broken pick" lore "A broken\npick" "ench unbreaking:1,effi:3"
    public void give(ParameterizedContext context)
    {
        User forUser = null;
        World forWorld = null;
        if (context.getSender() instanceof User)
        {
            forUser = (User)context.getSender();
            forWorld = ((User)context.getSender()).getWorld();
        }
        else if (context.hasParam("w"))
        {
            forWorld = context.getParam("w", null);
            if (forWorld == null)
            {
                context.sendTranslated(NEGATIVE, "Unknown World {input#world}!", context.getString("w"));
                return;
            }
        }
        if (context.hasArg(1))
        {
            forUser = context.getUser(1);
            if (forUser == null)
            {
                context.sendTranslated(NEGATIVE, "User {user} not found!", context.getString(1));
                return;
            }
        }
        else if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(NEGATIVE, "You need to specify a user");
            return;
        }
        ItemStack matchedItem = Match.material().itemStack(context.getString("i"));
        if (matchedItem == null)
        {
            context.sendTranslated(NEGATIVE, "Item {input#name} not found!", context.getString("i"));
            return;
        }
        ItemMeta itemMeta = matchedItem.getItemMeta();
        if (context.hasParam("n"))
        {
            itemMeta.setDisplayName(ChatFormat.parseFormats(context.getString("n")));
        }
        if (context.hasParam("l"))
        {
            itemMeta.setLore(asList(StringUtils.explode("\\n", ChatFormat.parseFormats(context.getString("l")))));
        }
        if (context.hasParam("e"))
        {
            String[] enchs = StringUtils.explode(",", context.getString("e"));
            for (String ench : enchs)
            {
                Enchantment enchantment;
                int power;
                if (ench.contains(":"))
                {
                    enchantment = Match.enchant().enchantment(ench.substring(0, ench.indexOf(":")));
                    if (enchantment == null)
                    {
                        context.sendTranslated(NEGATIVE, "Unknown Enchantment {input#enchant}", ench);
                        return;
                    }
                    power = Integer.parseInt(ench.substring(ench.indexOf(":")+1));
                }
                else
                {
                    enchantment = Match.enchant().enchantment(ench);
                    if (enchantment == null)
                    {
                        context.sendTranslated(NEGATIVE, "Unknown Enchantment {input#enchant}", ench);
                        return;
                    }
                    power = enchantment.getMaxLevel();
                }
                itemMeta.addEnchant(enchantment, power, true);
            }
        }
        matchedItem.setItemMeta(itemMeta);
        Integer amount = matchedItem.getMaxStackSize();
        if (context.hasParam("a"))
        {
            amount = context.getParam("a", null);
            if (amount == null)
            {
                context.sendTranslated(NEGATIVE, "Invalid amount {input#amount}", context.getString("a"));
                return;
            }
        }
        matchedItem.setAmount(amount);
        this.manager.giveItem(context.getSender(), forUser, forWorld, context.getString(0), matchedItem);
    }
}
