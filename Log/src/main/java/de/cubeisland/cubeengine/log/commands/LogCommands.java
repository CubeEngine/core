package de.cubeisland.cubeengine.log.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.log.Log;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;

public class LogCommands extends ContainerCommand
{
    public static final String toolName = ChatFormat.parseFormats("&9Logging-ToolBlock");
    //TODO additional lore lines for changing logging mode
    private Log module;

    public LogCommands(Log module)
    {
        super(module, "log", "log-module commands.");
        this.module = module;
    }

    @Command(desc = "Shows the current queue-size.")
    public void queuesize(CommandContext context)
    {
        int size = module.getLogManager().getQueueSize();
        if (size == 0)
        {
            context.sendMessage("log","&aLogging-queue is currently empty!");
        }
        else
        {
            context.sendMessage("log","&a%d logs are currently queued!",size);
        }
    }

    @Alias(names = "lb")
    @Command(desc = "Gives you a block to check logs with.",
    usage = "[log-type]", max = 2)
    public void block(CommandContext context)
    {
        Material blockMaterial = Material.BEDROCK;
        if (context.hasArg(0))
        {
            String match = Match.string().matchString(context.getString(0),"chest","container","lava","water","fire","player","kills","block");
            if (match.equals("chest") || match.equals("container"))
            {
                  blockMaterial = Material.CHEST;
            }
            else if (match.equals("lava"))
            {
                 blockMaterial = Material.STATIONARY_LAVA;
            }
            else if (match.equals("water"))
            {
                blockMaterial = Material.STATIONARY_WATER;
            }
            else if (match.equals("fire"))
            {
                blockMaterial = Material.FIRE;
            }
            else if (match.equals("player"))
            {
                blockMaterial = Material.PUMPKIN;
            }
            else if (match.equals("kills"))
            {
                blockMaterial = Material.SOUL_SAND;
            }
            else if (match.equals("block"))
            {
                blockMaterial = Material.LOG;
            }
        }
        //TODO show possible blocks in help
        // chest/container : lava : water : fire : player : kills : block
        if (context.getSender() instanceof User)
        {
            User user = (User) context.getSender();
            ItemStack found = null;
            for (ItemStack item : user.getInventory().getContents())
            {
                if (item != null && item.getType().equals(blockMaterial) && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(toolName))
                {
                    found = item;
                    break;
                }
            }
            if (found == null)
            {
                found = new ItemStack(blockMaterial,1);
                ItemMeta meta = found.getItemMeta();
                meta.setDisplayName(toolName);
                meta.setLore(Arrays.asList("created by "+user.getName()));
                found.setItemMeta(meta);
                ItemStack oldItemInHand = user.getItemInHand();
                user.setItemInHand(found);
                HashMap<Integer,ItemStack> tooMuch = user.getInventory().addItem(oldItemInHand);
                for (ItemStack item : tooMuch.values())
                {
                    user.getWorld().dropItemNaturally(user.getLocation(),item);
                }
                user.updateInventory();
                context.sendMessage("log","&aReveived a Log-Block!");
                return;
            }
            user.getInventory().removeItem(found);
            ItemStack oldItemInHand = user.getItemInHand();
            user.setItemInHand(found);
            user.getInventory().addItem(oldItemInHand);
            user.updateInventory();
            context.sendMessage("log","&aFound your Log-Block in your inventory!");
        }
        else
        {
            context.sendMessage("log","&cWhy don't you check in your log-file? You won't need a block there!");
        }
    }
}
