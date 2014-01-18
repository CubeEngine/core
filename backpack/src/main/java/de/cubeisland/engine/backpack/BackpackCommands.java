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

import java.util.Arrays;

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
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.matcher.Match;

import static de.cubeisland.engine.backpack.BackpackPermissions.OPEN_OTHER_USER;
import static de.cubeisland.engine.backpack.BackpackPermissions.OPEN_OTHER_WORLDS;

public class BackpackCommands extends ContainerCommand
{
    private BackpackManager manager;

    public BackpackCommands(Backpack module, BackpackManager manager)
    {
        super(module, "backpack", "The Backpack commands", Arrays.asList("bp"));
        this.manager = manager;
    }

    @Alias(names = "openbp")
    @Command(desc = "opens a backpack", usage = "<name> [user] [w <world>]",
             params = @Param(names = {"w", "world", "for", "in"},
                              completer = WorldCompleter.class, type = World.class),
             min = 1, max = 1)
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
                    context.sendTranslated("&cUser &2%s&c not found!", context.getString(1));
                    return;
                }
            }
            World forWorld = forUser.getWorld();
            if (context.hasParam("w"))
            {
                forWorld = context.getParam("w", null);
                if (forWorld == null)
                {
                    context.sendTranslated("&cUnknown World &6%s&c!", context.getString("w"));
                    return;
                }
            }
            if (context.getSender() != forUser && !OPEN_OTHER_USER.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to open the backpacks of other users!");
                return;
            }
            if (forUser.getWorld() != forWorld && ! OPEN_OTHER_WORLDS.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to open backpacks from an other world!");
                return;
            }
            manager.openBackpack((User)context.getSender(), forUser, forWorld, context.getString(0));
            return;
        }
        context.sendTranslated("&cYou cannot open a inventory in console!"); // TODO perhaps save inventory to yml
    }

    @Alias(names = "createbp")
    @Command(desc = "creates a new backpack",
             usage = "<name> [user] [-global]|[-single] [-blockinput] [w <world>] [p <pages>] [s <size>]",
             flags = {
                 @Flag(name = "g", longName = "global"),
                 @Flag(name = "s", longName = "single"),
                 @Flag(name = "b", longName = "blockinput")
             }
        , params = {@Param(names = {"w", "world", "for", "in"},
                  completer = WorldCompleter.class, type = World.class)
        ,@Param(names = {"p", "pages"}, type = Integer.class),
        @Param(names = {"s","size"}, type = Integer.class)},
             min = 1, max = 2)
    public void create(ParameterizedContext context)
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
                context.sendTranslated("&cUnknown World &6%s&c!", context.getString("w"));
                return;
            }
        }
        else if (!context.hasFlag("g"))
        {
            context.sendTranslated("&aYou have to specify a world for non global backpacks!");
            return;
        }
        if (context.hasArg(1))
        {
            forUser = context.getUser(1);
            if (forUser == null)
            {
                context.sendTranslated("&cUser &2%s&c not found!", context.getString(1));
                return;
            }
        }
        else if (!(context.getSender() instanceof User))
        {
            context.sendTranslated("&cYou need to specify a User");
            return;
        }
        manager.createBackpack(context.getSender(), forUser, context.getString(0), forWorld, context
            .hasFlag("g"), context.hasFlag("s"), context.hasFlag("b"), context.getParam("p", 1), context.getParam("s", 6));
    }

    @Alias(names = "modifybp")
    @Command(desc = "modifies a backpack",
             usage = "<name> [user] [w <world>] [pages <pages>] [s <size>] [blockinput <true|false>]",
    params = {
        @Param(names = {"p","pages"}, type = Integer.class),
        @Param(names = {"s","size"}, type = Integer.class),
        @Param(names = {"b","blockinput"}, type = Boolean.class),
        @Param(names = {"w", "world", "for", "in"},
               completer = WorldCompleter.class, type = World.class),
    }, min = 1, max = 2)
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
                context.sendTranslated("&cUnknown World &6%s&c!", context.getString("w"));
                return;
            }
        }
        else if (!context.hasFlag("g"))
        {
            context.sendTranslated("&aYou have to specify a world for non global backpacks!");
            return;
        }
        if (context.hasArg(1))
        {
            forUser = context.getUser(1);
            if (forUser == null)
            {
                context.sendTranslated("&cUser &2%s&c not found!", context.getString(1));
                return;
            }
        }
        else if (!(context.getSender() instanceof User))
        {
            context.sendTranslated("&cYou need to specify a User");
            return;
        }
        manager.modifyBackpack(context.getSender(), forUser, context.getString(0), forWorld,
                               (Integer)context.getParam("p", null),
                               (Boolean)context.getParam("b", null),
                               (Integer)context.getParam("s", null));
    }

    @Alias(names = "givebp")
    @Command(desc = "Puts items into a backpack",
             usage = "<name> [user] [w <world>] <item <item>[:data]> [name <name>] [lore <loreline>[,<loreline>]] [ench <enchs...>] [amount <amount>]",
    params = {@Param(names = {"i","item"}, required = true),
              @Param(names = {"n","name"}),
              @Param(names = {"l","lore"}),
              @Param(names = {"l","lore"}),
              @Param(names = {"a", "amount"}, type = Integer.class),
              @Param(names = {"e","ench", "enchantments"}),
              @Param(names = {"w", "world", "for", "in"},
                     completer = WorldCompleter.class, type = World.class)
    }, max = 2, min = 1)
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
                context.sendTranslated("&cUnknown World &6%s&c!", context.getString("w"));
                return;
            }
        }
        if (context.hasArg(1))
        {
            forUser = context.getUser(1);
            if (forUser == null)
            {
                context.sendTranslated("&cUser &2%s&c not found!", context.getString(1));
                return;
            }
        }
        else if (!(context.getSender() instanceof User))
        {
            context.sendTranslated("&cYou need to specify a User");
            return;
        }
        ItemStack matchedItem = Match.material().itemStack(context.getString("i"));
        if (matchedItem == null)
        {
            context.sendTranslated("&cCould not match item &6%s", context.getString("i"));
            return;
        }
        ItemMeta itemMeta = matchedItem.getItemMeta();
        if (context.hasParam("n"))
        {
            itemMeta.setDisplayName(ChatFormat.parseFormats(context.getString("n")));
        }
        if (context.hasParam("l"))
        {
            itemMeta.setLore(Arrays.asList(StringUtils.explode("\\n", ChatFormat.parseFormats(context.getString("l")))));
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
                        context.sendTranslated("&cUnknown Enchantment &6%s", ench);
                        return;
                    }
                    power = Integer.parseInt(ench.substring(ench.indexOf(":")+1));
                }
                else
                {
                    enchantment = Match.enchant().enchantment(ench.substring(0, ench.indexOf(":")));
                    if (enchantment == null)
                    {
                        context.sendTranslated("&cUnknown Enchantment &6%s", ench);
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
                context.sendTranslated("&cInvalid amount &6%s", context.getString("a"));
                return;
            }
        }
        matchedItem.setAmount(amount);
        this.manager.giveItem(context.getSender(), forUser, forWorld, context.getString(0), matchedItem);
    }
}
