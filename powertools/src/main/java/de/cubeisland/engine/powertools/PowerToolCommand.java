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
package de.cubeisland.engine.powertools;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.engine.core.command.ArgBounds;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.matcher.Match;

import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;
import static java.util.Arrays.asList;

/**
 * The PowerTool commands allow binding commands and/or chat-macros to a specific item.
 * <p>The data is appended onto the items lore
 * <p>/powertool
 */
public class PowerToolCommand extends ContainerCommand implements Listener
{
    public PowerToolCommand(Module module)
    {
        super(module, "powertool", "Binding shortcuts to an item.", asList("pt"));
        this.getContextFactory().setArgBounds(new ArgBounds(0, NO_MAX));

        this.delegateChild(new MultiContextFilter() {
            @Override
            public String getChild(CommandContext context)
            {
                if (context.hasArg(0))
                {
                    return "add"; // acts as /pt add with -r flag
                }
                return "clear"; // /pt without params clears the tool
            }

            @Override
            public CommandContext filterContext(CommandContext context, String child)
            {
                ParameterizedContext pContext = (ParameterizedContext)context;
                Set<String> flagSet = pContext.getFlags();
                if (child.equals("add"))
                {
                    flagSet.add("r");
                }
                return new ParameterizedContext(context.getCommand(), context.getSender(), context.getLabels(), context.getArgs(), flagSet, pContext.getParams());
            }
        });
    }

    @Alias(names = "ptc")
    @Command(desc = "Removes all command from your powertool",
             flags = @Flag(longName = "all", name = "a"), usage = "[-a]")
    public void clear(ParameterizedContext context)
    {
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            User user = (User)sender;
            if (context.hasFlag("a"))
            {
                for (ItemStack item : user.getInventory().getContents())
                {
                    this.setPowerTool(item, null);
                }
                context.sendTranslated("&aRemoved all commands bound to items in your inventory!");
            }
            else
            {
                if (user.getItemInHand().getTypeId() == 0)
                {
                    context.sendTranslated("&eYou are not holding any item in your hand.");
                    return;
                }
                this.setPowerTool(user.getItemInHand(), null);
                context.sendTranslated("&aRemoved all commands bound to the item in your hand!");
            }
            return;
        }
        context.sendTranslated("&eNo more power for you!");
    }

    @Alias(names = "ptr")
    @Command(names = {
        "remove", "del", "delete", "rm"
    }, desc = "Removes a command from your powertool", flags = @Flag(longName = "chat", name = "c"), usage = "[command] [-chat]", max = NO_MAX)
    public void remove(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (sender.getItemInHand().getTypeId() == 0)
            {
                context.sendTranslated("&eYou are not holding any item in your hand.");
                return;
            }
            String cmd = context.getStrings(0);
            this.remove(context, sender.getItemInHand(), cmd, !context.hasFlag("c"));
            return;
        }
        context.sendTranslated("&eNo more power for you!");
    }

    private void remove(CommandContext context, ItemStack item, String cmd, boolean isCommand)
    {
        List<String> powertools = this.getPowerTools(item);
        if (cmd == null || cmd.isEmpty())
        {
            powertools.remove(powertools.size() - 1);
            this.setPowerTool(item, powertools);
            context.sendTranslated("&aRemoved the last command bound to this item!");
        }
        else
        {
            if (isCommand)
            {
                cmd = "/" + cmd;
            }
            boolean removed = false;
            while (powertools.remove(cmd)) // removes also multiple same cmds
            {
                removed = true;
            }
            if (removed)
            {
                context.sendTranslated("&aRemoved the command: &e%s &abound to this item!", cmd);
            }
            else
            {
                context.sendTranslated("&cThe command &e%s &cwas not found on this item!", cmd);
            }
        }
        this.setPowerTool(item, powertools);
        if (powertools.isEmpty())
        {
            context.sendTranslated("&eNo more commands saved on this item!");
            return;
        }
        this.showPowerToolList(context, powertools, false, false);
    }

    @Alias(names = "pta")
    @Command(desc = "Adds a command to your powertool", flags = {
        @Flag(longName = "chat", name = "c"),
        @Flag(longName = "replace", name = "r")
    }, usage = "<commandstring>", min = 1, max = NO_MAX)
    public void add(ParameterizedContext context)
    {
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            User user = (User)sender;
            String cmd = context.getStrings(0);
            if (user.getItemInHand().getType().equals(Material.AIR))
            {
                user.sendTranslated("&eYou do not have an item in your hand to bind the command to!");
                return;
            }
            if (!context.hasFlag("c"))
            {
                cmd = "/" + cmd;
            }
            List<String> powerTools;
            if (context.hasFlag("r"))
            {
                powerTools = new ArrayList<>(1);
            }
            else
            {
                powerTools = this.getPowerTools(user.getItemInHand());
            }
            powerTools.add(cmd);
            this.setPowerTool(user.getItemInHand(), powerTools);
            return;
        }
        context.sendTranslated("&eYou already have enough power!");
    }

    @Alias(names = "ptl")
    @Command(desc = "Lists your powertool-bindings.", flags = @Flag(longName = "all", name = "a"))
    public void list(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (context.hasFlag("a"))
            {
                for (ItemStack item : sender.getInventory().getContents())
                {
                    String itemName = item.getItemMeta().getDisplayName();
                    if (itemName == null)
                    {
                        sender.sendMessage("&6" + Match.material().getNameFor(item) + "&6:");
                    }
                    else
                    {
                        sender.sendMessage("&6" + itemName + "&6:");
                    }
                    this.showPowerToolList(context, this.getPowerTools(item), false, false);
                }
                return;
            }
            if (sender.getItemInHand().getType().equals(Material.AIR))
            {
                context.sendTranslated("&eYou do not have an item in your hand.");
            }
            else
            {
                this.showPowerToolList(context, this.getPowerTools(sender.getItemInHand()), false, true);
            }
            return;
        }
        context.sendTranslated("&eYou already have enough power!");
    }

    private void showPowerToolList(CommandContext context, List<String> powertools, boolean lastAsNew, boolean showIfEmpty)
    {
        if ((powertools == null || powertools.isEmpty()))
        {
            if (showIfEmpty)
            {
                context.sendTranslated("&cNo commands saved on this item!");
            }
            return;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (; i < powertools.size() - 1; i++)
        {
            sb.append("\n&f").append(powertools.get(i));
        }
        if (lastAsNew)
        {
            context.sendTranslated("&6%d &ecommand(s) bound to this item:%s\n&e%s &6(&aNEW&6)", i + 1, sb.toString(), powertools.get(i));
        }
        else
        {
            context.sendTranslated("&6%d &ecommand(s) bound to this item:%s\n&f%s", i + 1, sb.toString(), powertools.get(i));
        }
    }

    private void setPowerTool(ItemStack item, List<String> newPowerTools)
    {
        ItemMeta meta = item.getItemMeta();
        List<String> newLore = new ArrayList<>();
        if (meta.hasLore())
        {
            for (String line : meta.getLore())
            {
                if (line.equals("§2PowerTool"))
                {
                    break;
                }
                newLore.add(line);
            }
        }
        if (newPowerTools != null && !newPowerTools.isEmpty())
        {
            newLore.add(ChatFormat.parseFormats("&2PowerTool"));
            newLore.addAll(newPowerTools);
        }
        meta.setLore(newLore);
        item.setItemMeta(meta);
    }

    /**
     * Gets the PowerTools saved on this item.
     *
     * @param item
     * @return a list of the saved commands and/or chat-macros
     */
    private List<String> getPowerTools(ItemStack item)
    {
        ItemMeta meta = item.getItemMeta();
        List<String> powerTool = new ArrayList<>();
        if (meta.hasLore())
        {
            boolean ptStart = false;
            for (String line : meta.getLore())
            {
                if (!ptStart && line.equals("§2PowerTool"))
                {
                    ptStart = true;
                }
                else if (ptStart)
                {
                    powerTool.add(line);
                }
            }
        }
        return powerTool;
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            Player player = event.getPlayer();
            if (!player.getItemInHand().getType().equals(Material.AIR)
                    && PowertoolsPerm.POWERTOOL_USE.isAuthorized(event.getPlayer()))
            {
                List<String> powerTool = this.getPowerTools(player.getItemInHand());
                for (String command : powerTool)
                {
                    player.chat(command);
                }
                if (!powerTool.isEmpty())
                {
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setCancelled(true);
                }
            }
        }
    }
}
