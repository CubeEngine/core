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
package de.cubeisland.cubeengine.basics.command.moderation.kit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.FileUtil;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;

public class KitCommand extends ContainerCommand
{
    public KitCommand(Basics module)
    {
        super(module, "kit", "Manages item-kits");
        this.module = module;
    }
    private Basics module;

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (context.hasArg(0))
        {
            this.give((ParameterizedContext)context);//TODO this works but not used as intended!?
            return null;
        }
        else
        {
            return super.run(context);
        }
    }

    @Command(desc = "Creates a new kit with the items in your inventory.",
            flags = @Flag(longName = "toolbar", name = "t"),
            usage = "<kitName> [-toolbar]", min = 1, max = 1)
    public void create(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendTranslated("&cJust log in or use the config!");
            return;
        }
        List<KitItem> itemList = new ArrayList<KitItem>();
        if (context.hasFlag("t"))
        {
            ItemStack[] items = sender.getInventory().getContents();
            for (int i = 0; i <= 8; ++i)
            {
                if (items[i] == null || items[i].getTypeId() == 0)
                {
                    break;
                }
                itemList.add(
                        new KitItem(items[i].getType(),
                            items[i].getDurability(),
                            items[i].getAmount(),
                            items[i].getItemMeta().getDisplayName()));
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
                            item.getItemMeta().getDisplayName()));
            }
        }
        Kit kit = new Kit(this.module,context.getString(0), false, 0, -1, true, "", new ArrayList<String>(), itemList);
        if (!FileUtil.isValidFileName(kit.getKitName()))
        {
            context.sendTranslated("&6%s &cis is not a valid name! Do not use characters like *, | or ?", kit.getKitName());
            return;
        }
        module.getKitManager().saveKit(kit);
        if (kit.getPermission() != null)
        {
            module.getCore().getPermissionManager().registerPermission(module,kit.getPermission());
        }
        context.sendTranslated("&aCreated the &6%s &akit!", kit.getKitName());
    }

    @Alias(names = "kit")
    @Command(desc = "Gives a kit of items.", usage = "<kitname> [player]", min = 1, max = 2, flags = {
        @Flag(longName = "all", name = "a"),
        @Flag(longName = "force", name = "f")
    })
    public void give(ParameterizedContext context)
    {
        String kitname = context.getString(0);
        User user = null;
        Kit kit = module.getKitManager().getKit(kitname);
        boolean force = false;
        if (context.hasFlag("f") && BasicsPerm.COMMAND_KIT_GIVE_FORCE.isAuthorized(context.getSender()))
        {
            force = true;
        }
        if (kit == null)
        {
            context.sendTranslated("&cKit &6%s &cnot found!", kitname);
            return;
        }
        if (context.hasFlag("a"))
        {
            boolean gaveKit = false;
            int kitNotreceived = 0;
            for (User receiver : module.getCore().getUserManager().getOnlineUsers())
            {
                try
                {
                    if (kit.give(context.getSender(), receiver, force))
                    {
                        if (receiver.getName().equals(context.getSender().getName()))
                        {
                            context.sendTranslated("&aReceived the &6%s &akit!", kit.getKitName());
                        }
                        else
                        {
                            context.sendTranslated("&aYou gave &2%s &athe &6%s &akit!", receiver.getName(), kit.getKitName());
                            receiver.sendTranslated("&aReceived the &6%s &akit. Enjoy it!", kit.getKitName());
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
                context.sendTranslated("&cNo one received the kit!");
            }
            else if (kitNotreceived > 0)
            {
                context.sendTranslated("&c%d players did not receive a kit!");
            }
        }
        else
        {
            boolean other = false;
            if (context.hasArg(1))
            {
                user = context.getUser(1);
                other = true;
            }
            else if (context.getSender() instanceof User)
            {
                user = (User)context.getSender();
            }
            if (user == null)
            {
                context.sendTranslated("&cUser %s &cnot found!", context.getString(0));
                return;
            }
            if (kit.give(context.getSender(), user, force))
            {
                if (!other)
                {
                    if (kit.getCustomMessage().equals(""))
                    {
                        context.sendTranslated("&aReceived the &6%s &akit. Enjoy it!", kit.getKitName());
                    }
                    else
                    {
                        context.sendMessage(kit.getCustomMessage());
                    }
                }
                else
                {
                    context.sendTranslated("&aYou gave &2%s &athe &6%s &akit!", user.getName(), kit.getKitName());
                    if (kit.getCustomMessage().equals(""))
                    {
                        user.sendTranslated("&aReceived the &6%s &akit. Enjoy it!", kit.getKitName());
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
                    context.sendTranslated("&2%s &ehas not enough inventory-space for this kit!", user.getName());
                }
                else
                {
                    context.sendTranslated("&eYou don't have enough inventory-space for this kit!");
                }
            }
        }
    }
}
