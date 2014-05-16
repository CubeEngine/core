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
package de.cubeisland.engine.log.commands;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.LogAttachment;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.Material.WOOD_AXE;

public class LogCommands extends ContainerCommand
{
    public static final String toolName = ChatFormat.INDIGO + "Logging-ToolBlock";
    public static final String selectorToolName = ChatFormat.INDIGO + "Selector-Tool";

    private final Log module;

    public LogCommands(Log module)
    {
        super(module, "log", "log-module commands.");
        this.module = module;
    }

    @SuppressWarnings("deprecation")
    public static void giveSelectionTool(User user)
    {
        ItemStack found = null;
        for (ItemStack item : user.getInventory().getContents())
        {
            if (item != null && item.getType() == WOOD_AXE
                && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(selectorToolName))
            {
                found = item;
                break;
            }
        }
        if (found == null)
        {
            found = new ItemStack(WOOD_AXE, 1);
            ItemMeta meta = found.getItemMeta();
            meta.setDisplayName(selectorToolName);
            meta.setLore(Arrays.asList("created by " + user.getDisplayName()));
            found.setItemMeta(meta);
            ItemStack oldItemInHand = user.getItemInHand();
            user.setItemInHand(found);
            HashMap<Integer, ItemStack> tooMuch = user.getInventory().addItem(oldItemInHand);
            for (ItemStack item : tooMuch.values())
            {
                user.getWorld().dropItemNaturally(user.getLocation(), item);
            }
            user.updateInventory();
            user.sendTranslated(POSITIVE, "Received a new Region-Selector Tool");
            return;
        }
        user.getInventory().removeItem(found);
        ItemStack oldItemInHand = user.getItemInHand();
        user.setItemInHand(found);
        user.getInventory().addItem(oldItemInHand);
        user.updateInventory();
        user.sendTranslated(POSITIVE, "Found a Region-Selector Tool in your inventory!");
    }


    // TODO command to show current params on a lookup-tool
    // TODO command to change params on a lookup-tool (only further limiting)

    //TODO add rollback tool
    //TODO loghand (cmd hand) -> toggles general lookup with bare hands

    @Command(desc = "Shows the current queue-size.")
    public void queuesize(CommandContext context)
    {
        int size = module.getLogManager().getQueueSize();
        if (size == 0)
        {
            context.sendTranslated(POSITIVE, "Logging-queue is currently empty!");
        }
        else
        {
            context.sendTranslated(POSITIVE, "{integer} logs are currently queued!", size);
        }
        this.module.getLogManager().getQueryManager().logStatus();
    }

    private Material matchType(String type, boolean block)// or item
    {
        if (type == null)
        {
            if (block)
            {
                return Material.BEDROCK;
            }
            return Material.BOOK;
        }
        String match = Match.string().matchString(type, "chest", "player", "kills", "block");
        if (match == null)
        {
            return null;
        }
        if (match.equals("chest") || match.equals("container"))
        {
            if (block)
            {
                return Material.CHEST;
            }
            return Material.CLAY_BRICK;
        }
        if (match.equals("player"))
        {
            if (block)
            {
                return Material.PUMPKIN;
            }
            return Material.CLAY_BALL;
        }
        if (match.equals("kills"))
        {
            if (block)
            {
                return Material.SOUL_SAND;
            }
            return Material.BONE;
        }
        if (match.equals("block"))
        {
            if (block)
            {
                return Material.LOG;
            }
            return Material.NETHER_BRICK_ITEM;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    private void findLogTool(User user, Material material)
    {
        ItemStack found = null;
        for (ItemStack item : user.getInventory().getContents())
        {
            if (item != null && item.getType().equals(material) && item.hasItemMeta() && item.getItemMeta()
                                                                                             .hasDisplayName() && item
                .getItemMeta().getDisplayName().equals(toolName))
            {
                found = item;
                break;
            }
        }
        if (found == null)
        {
            found = new ItemStack(material, 1);
            ItemMeta meta = found.getItemMeta();
            meta.setDisplayName(toolName);
            meta.setLore(Arrays.asList("created by " + user.getDisplayName()));
            found.setItemMeta(meta);
            ItemStack oldItemInHand = user.getItemInHand();
            user.setItemInHand(found);
            HashMap<Integer, ItemStack> tooMuch = user.getInventory().addItem(oldItemInHand);
            for (ItemStack item : tooMuch.values())
            {
                user.getWorld().dropItemNaturally(user.getLocation(), item);
            }
            user.updateInventory();
            user.sendTranslated(POSITIVE, "Received a new Log-Tool!");
            LogAttachment logAttachment = user.attachOrGet(LogAttachment.class, this.module);
            logAttachment.createNewLookup(material);

            return;
        }
        user.getInventory().removeItem(found);
        ItemStack oldItemInHand = user.getItemInHand();
        user.setItemInHand(found);
        user.getInventory().addItem(oldItemInHand);
        user.updateInventory();
        user.sendTranslated(POSITIVE, "Found a Log-Tool in your inventory!");
    }

    @Alias(names = "lb")
    @Command(desc = "Gives you a block to check logs with." +
        "no log-type: Shows everything\n" +
        "chest: Shows chest-interactions only\n" +
        "player: Shows player-interacions only\n" +
        "kills: Shows kill-interactions only\n" +
        "block: Shows block-changes only",
             indexed = @Grouped(req = false, value = @Indexed(label = "log-type")))
    public void block(CommandContext context)
    {
        //TODO tabcompleter for logBlockTypes (waiting for CE-389)
        if (context.getSender() instanceof User)
        {
            Material blockMaterial = this.matchType(context.<String>getArg(0), true);
            if (blockMaterial == null)
            {
                context
                    .sendTranslated(NEGATIVE, "{input} is not a valid log-type. Use chest, container, player, block or kills instead!", context
                        .getArg(0));
                return;
            }
            User user = (User)context.getSender();
            this.findLogTool(user, blockMaterial);
        }
        else
        {
            context
                .sendTranslated(NEGATIVE, "Why don't you check in your log-file? You won't need a block there!");
        }
    }

    @Alias(names = "lt")
    @Command(desc = "Gives you an item to check logs with.\n" +
        "no log-type: Shows everything\n" +
        "chest: Shows chest-interactions only\n" +
        "player: Shows player-interacions only\n" +
        "kills: Shows kill-interactions only\n" +
        "block: Shows block-changes only",
             indexed = @Grouped(req = false, value = @Indexed(label = "log-type")))
    public void tool(CommandContext context)
    {
        //TODO tabcompleter for logToolTypes (waiting for CE-389)
        if (context.getSender() instanceof User)
        {
            Material blockMaterial = this.matchType(context.<String>getArg(0), false);
            if (blockMaterial == null)
            {
                context.sendTranslated(NEGATIVE, "{input} is not a valid log-type. Use chest, container, player, block or kills instead!", context.getArg(
                    0));
                return;
            }
            User user = (User)context.getSender();
            this.findLogTool(user, blockMaterial);
        }
        else
        {
            context.sendTranslated(NEGATIVE, "Why don't you check in your log-file? You won't need a block there!");
        }
    }

    @Command(desc = "Gives you a item to select a region with.")
    public void selectionTool(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            giveSelectionTool((User)context.getSender());
        }
        else
        {
            context.sendTranslated(NEGATIVE, "You cannot hold a selection tool!");
        }
        // if worldEdit give WE wand else give OUR wand
    }
}
