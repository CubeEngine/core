package de.cubeisland.cubeengine.log.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;

public class LogCommands extends ContainerCommand
{
    public static final String toolName = ChatFormat.parseFormats("&9Logging-ToolBlock");

    public LogCommands(Module module)
    {
        super(module, "log", "log-module commands.");
    }

    @Alias(names = "lb")
    @Command(desc = "Gives you a block to check logs with.")
    public void block(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User user = (User) context.getSender();
            ItemStack found = null;
            for (ItemStack item : user.getInventory().getContents())
            {
                if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(toolName))
                {
                    found = item;
                    break;
                }
            }
            if (found == null)
            {
                found = new ItemStack(Material.BEDROCK,1);
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
