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
package de.cubeisland.engine.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.reflected.context.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.context.Flags;
import de.cubeisland.engine.core.command.reflected.context.Grouped;
import de.cubeisland.engine.core.command.reflected.context.IParams;
import de.cubeisland.engine.core.command.reflected.context.Indexed;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.FileUtil;

import static de.cubeisland.engine.core.util.ChatFormat.WHITE;
import static de.cubeisland.engine.core.util.ChatFormat.YELLOW;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static org.bukkit.Material.AIR;

public class KitCommand extends ContainerCommand
{
    private final KitManager manager;
    private final Kits module;

    public KitCommand(Kits module)
    {
        super(module, "kit", "Manages item-kits");
        this.module = module;
        this.manager = module.getKitManager();
        // TODO this.getContextFactory().setArgBounds(new ArgBounds(0, 2));
        this.delegateChild(new DelegatingContextFilter()
        {
            @Override
            public String delegateTo(CommandContext context)
            {
                return context.hasArg(0) ? "give" : null;
            }
        });
    }

    @Command(desc = "Creates a new kit with the items in your inventory.")
    @IParams(@Grouped(@Indexed(label = "kitname")))
    @Flags(@Flag(longName = "toolbar", name = "t"))
    public void create(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendTranslated(NEGATIVE, "Just log in or use the config!");
            return;
        }
        List<KitItem> itemList = new ArrayList<>();
        if (context.hasFlag("t"))
        {
            ItemStack[] items = sender.getInventory().getContents();
            for (int i = 0; i <= 8; ++i)
            {
                if (items[i] == null || items[i].getType() == AIR)
                {
                    break;
                }
                itemList.add(
                        new KitItem(items[i].getType(),
                            items[i].getDurability(),
                            items[i].getAmount(),
                            items[i].getItemMeta().getDisplayName(),
                            items[i].getEnchantments()));
            }
        }
        else
        {
            for (ItemStack item : sender.getInventory().getContents())
            {
                if (item == null || item.getTypeId() == 0)
                {
                    break;
                }
                itemList.add(
                        new KitItem(item.getType(),
                            item.getDurability(),
                            item.getAmount(),
                            item.getItemMeta().getDisplayName(),
                            item.getEnchantments()));
            }
        }
        Kit kit = new Kit(module, context.<String>getArg(0), false, 0, -1, true, "", new ArrayList<String>(), itemList);
        if (!FileUtil.isValidFileName(kit.getKitName()))
        {
            context.sendTranslated(NEGATIVE, "{name#kit} is is not a valid name! Do not use characters like *, | or ?", kit.getKitName());
            return;
        }
        manager.saveKit(kit);
        if (kit.getPermission() != null)
        {
            getModule().getCore().getPermissionManager().registerPermission(getModule(), kit.getPermission());
        }
        context.sendTranslated(POSITIVE, "Created the {name#kit} kit!", kit.getKitName());
    }


    @Alias(names = "kitlist")
    @Command(desc = "Lists all currently available kits.")
    public void list(ParameterizedContext context)
    {
        context.sendTranslated(POSITIVE, "The following kits are available:");
        String format = "  " + WHITE + "-" + YELLOW;
        for (String kitName : manager.getKitsNames())
        {
            context.sendMessage(format + kitName);
        }
    }

    @Alias(names = "kit")
    @Command(desc = "Gives a set of items.")
    @IParams({@Grouped(@Indexed(label = "kitname")),
              @Grouped(req = false, value =  @Indexed(label = "player", type = User.class))})
    @Flags({@Flag(longName = "all", name = "a"),
            @Flag(longName = "force", name = "f")})
    public void give(ParameterizedContext context)
    {
        String kitname = context.getArg(0);
        User user;
        Kit kit = manager.getKit(kitname);
        boolean force = false;
        if (context.hasFlag("f") && module.perms().COMMAND_KIT_GIVE_FORCE.isAuthorized(context.getSender()))
        {
            force = true;
        }
        if (kit == null)
        {
            context.sendTranslated(NEGATIVE, "Kit {input} not found!", kitname);
            return;
        }
        if (context.hasFlag("a"))
        {
            boolean gaveKit = false;
            int kitNotreceived = 0;
            for (User receiver : getModule().getCore().getUserManager().getOnlineUsers())
            {
                try
                {
                    if (kit.give(context.getSender(), receiver, force))
                    {
                        if (receiver.equals(context.getSender()))
                        {
                            context.sendTranslated(POSITIVE, "Received the {name#kit} kit!", kit.getKitName());
                        }
                        else
                        {
                            context.sendTranslated(POSITIVE, "You gave {user} the {name#kit} kit!", receiver, kit.getKitName());
                            receiver.sendTranslated(POSITIVE, "Received the {name#kit} kit. Enjoy.", kit.getKitName());
                        }
                        gaveKit = true;
                    }
                }
                catch (Exception ex)
                {
                    kitNotreceived++;
                }
            }
            if (!gaveKit)
            {
                context.sendTranslated(NEGATIVE, "No one received the kit!");
            }
            else if (kitNotreceived > 0)
            {
                context.sendTranslated(NEGATIVE, "{amount} players did not receive a kit!", kitNotreceived); // TODO Have a string for if there is only one player, so non-plural
            }
        }
        else
        {
            boolean other = false;
            if (context.hasArg(1))
            {
                user = context.getArg(1);
                other = true;
            }
            else if (context.getSender() instanceof User)
            {
                user = (User)context.getSender();
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You need to specify a player!");
                return;
            }
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getArg(1));
                return;
            }
            if (!user.isOnline())
            {
                context.sendTranslated(NEGATIVE, "{user} is not online!", user.getDisplayName());
                return;
            }
            if (kit.give(context.getSender(), user, force))
            {
                if (!other)
                {
                    if (kit.getCustomMessage().equals(""))
                    {
                        context.sendTranslated(POSITIVE, "Received the {name#kit} kit. Enjoy.", kit.getKitName());
                    }
                    else
                    {
                        context.sendMessage(kit.getCustomMessage());
                    }
                }
                else
                {
                    context.sendTranslated(POSITIVE, "You gave {user} the {name#kit} kit!", user, kit.getKitName());
                    if (kit.getCustomMessage().equals(""))
                    {
                        user.sendTranslated(POSITIVE, "Received the {name#kit} kit. Enjoy.", kit.getKitName());
                    }
                    else
                    {
                        user.sendMessage(kit.getCustomMessage());
                    }
                }
            }
            else
            {
                if (other)
                {
                    context.sendTranslated(NEUTRAL, "{user} has not enough space in your inventory for this kit!", user);
                }
                else
                {
                    context.sendTranslated(NEUTRAL, "You don't have enough space in your inventory for this kit!");
                }
            }
        }
    }
}
