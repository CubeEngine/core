package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.blockCommand;
import static java.util.Arrays.asList;

/**
 * The powertool command allows binding commands/chatmacros to a specific item
 * using NBT-Data. /powertool
 */
public class PowerToolCommand extends ContainerCommand
{
    public PowerToolCommand(Module module)
    {
        super(module, "powertool", "Binding shortcuts to an item.", asList("pt"));
    }

    // TODO horrible broken
    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (context.hasArg(0))
        {
            //TODO change context to have the replace flag set
            this.add((ParameterizedContext)context);
            return null;
        }
        return super.run(context);
    }

    @Alias(names = "ptc")
    @Command(desc = "Removes all command from your powertool", flags = @Flag(longName = "all", name = "a"), usage = "[-a]")
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
                context.sendMessage("basics", "&aRemoved all commands bound to items in your inventory!");
            }
            else
            {
                if (user.getItemInHand().getTypeId() == 0)
                {
                    context.sendMessage("basics", "&eYou are not holding any item in your hand.");
                    return;
                }
                this.setPowerTool(user.getItemInHand(), null);
            }
        }
        else
        {
            context.sendMessage("basics", "&eNo more power for you!");
        }
    }

    @Alias(names = "ptr")
    @Command(names = {
        "remove", "del", "delete", "rm"
    }, desc = "Removes a command from your powertool", flags = @Flag(longName = "chat", name = "c"), usage = "[command] [-chat]")
    public void remove(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendMessage("basics", "&eNo more power for you!");
            return;
        }
        if (sender.getItemInHand().getTypeId() == 0)
        {
            context.sendMessage("basics", "&eYou are not holding any item in your hand.");
            return;
        }
        if (context.hasArg(0))
        {
            String cmd = context.getStrings(0);
            if (!context.hasFlag("c"))
            {
                cmd = "/" + cmd;
            }
            this.remove(context, sender.getItemInHand(), cmd);
        }
        else
        {
            this.remove(context, sender.getItemInHand(), null);
        }
    }

    private void remove(CommandContext context, ItemStack item, String cmd)
    {
        List<String> powertools = this.getPowerTools(item);
        boolean removed = false;
        if (cmd == null)
        {
            powertools.remove(powertools.size() - 1);
            context.sendMessage("basics", "&aRemoved the last command bound to this item!");
        }
        else
        {
            while (powertools.remove(cmd))
            {
                removed = true;
            }
            if (removed)
            {
                context.sendMessage("basics", "&aRemoved the command: &e%s &abound to this item!", cmd);
            }
            else
            {
                context.sendMessage("basics", "&cThe command &e%s &cwas not found on this item!", cmd);
            }
        }
        this.setPowerTool(item, powertools);
        if (powertools.isEmpty())
        {
            context.sendMessage("basics", "&eNo more commands saved on this item!");

        }
        else
        {
            this.printList(context, powertools, false, false);
        }
    }

    @Alias(names = "pta")
    @Command(desc = "Adds a command to your powertool", flags = {
        @Flag(longName = "chat", name = "c"),
        @Flag(longName = "replace", name = "r")
    }, usage = "<commandstring>", min = 1)
    public void add(ParameterizedContext context)
    {
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            User user = (User)sender;
            String cmd = context.getStrings(0);
            if (user.getItemInHand().getType().equals(Material.AIR))
            {
                user.sendMessage("basics", "&eYou do not have an item in your hand to bind the command to!");
                return;
            }
            if (!context.hasFlag("c"))
            {
                cmd = "/" + cmd;
            }
            List<String> powerTools;
            if (context.hasFlag("r"))
            {
                powerTools = new ArrayList<String>(1);
            }
            else
            {
                powerTools = this.getPowerTools(user.getItemInHand());
            }
            powerTools.add(cmd);
            this.setPowerTool(user.getItemInHand(), powerTools);
        }
        else
        {
            context.sendMessage("basics", "&eYou already have enough power!");
        }
    }

    @Alias(names = "ptl")
    @Command(desc = "Lists your powertool-bindings.", flags = @Flag(longName = "all", name = "a"))
    public void list(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendMessage("basics", "&eYou already have enough power!");
            return;
        }
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
                this.printList(context, this.getPowerTools(item), false, false);
            }
        }
        else
        {
            if (sender.getItemInHand().getType().equals(Material.AIR))
            {
                blockCommand(context, "basics", "&eYou do not have an item in your hand.");
            }
            this.printList(context, this.getPowerTools(sender.getItemInHand()), false, true);
        }
    }

    private void printList(CommandContext context, List<String> powertools, boolean lastAsNew, boolean showIfEmpty)
    {
        if ((powertools == null || powertools.isEmpty()) && showIfEmpty)
        {
            context.sendMessage("basics", "&cNo commands saved on this item!");
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
            context.sendMessage("basics", "&6%d &ecommand(s) bound to this item:%s\n&e%s &6(&aNEW&6)", i + 1, sb.toString(), powertools.get(i));
        }
        else
        {
            context.sendMessage("basics", "&6%d &ecommand(s) bound to this item:%s\n&f%s", i + 1, sb.toString(), powertools.get(i));
        }
    }

    private void setPowerTool(ItemStack item, List<String> newPowerTools)
    {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        List<String> newLore = new ArrayList<String>();
        if (lore != null)
        {
            for (String l : lore)
            {
                if (l.equals("§2PowerTool"))
                {
                    break;
                }
                newLore.add(l);
            }
        }
        newLore.add(ChatFormat.parseFormats("&2PowerTool"));
        if (newPowerTools != null)
        {
            newLore.addAll(newPowerTools);
        }
        meta.setLore(newLore);
        item.setItemMeta(meta);
    }

    private List<String> getPowerTools(ItemStack item)
    {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        List<String> powerTool = new ArrayList<String>();
        boolean ptStart = false;
        if (lore != null)
        {
            for (String l : lore)
            {
                if (!ptStart)
                {
                    if (l.equals("§2PowerTool"))
                    {
                        ptStart = true;
                    }
                }
                else
                {
                    powerTool.add(l);
                }
            }
        }
        return powerTool;
    }
}
