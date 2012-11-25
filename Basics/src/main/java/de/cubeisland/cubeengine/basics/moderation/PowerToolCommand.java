package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.blockCommand;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * The powertool command allows binding commands/chatmakros to a specific item
 * using NBT-Data. /powertool
 */
public class PowerToolCommand extends ContainerCommand
{
    public PowerToolCommand(Module module)
    {
        super(module, "pt", "Binding shortcuts to an item.", "powertool");
    }

    @Alias(names = "ptc")
    @Command(desc = "Removes all command from your powertool", flags = @Flag(longName = "all", name = "a"), usage = "[-a]")
    public void clear(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eNo more power for you!");
        if (context.hasFlag("a"))
        {
            for (ItemStack item : sender.getInventory().getContents())
            {
                NBTTagCompound tag = this.getTag(item, false);
                if (tag == null)
                {
                    continue;
                }
                tag.set("powerToolCommands", new NBTTagList());
                String nameToRemove = BukkitUtils.getItemStackLore(item).get(0);
                if (nameToRemove == null || nameToRemove.startsWith("§cPowertool:"))
                {
                    BukkitUtils.renameItemStack(sender.getItemInHand(), true, (String[]) null);
                }
            }
            context.sendMessage("basics", "&aRemoved all commands bound to item in your inventory!");
        }
        else
        {
            NBTTagCompound tag = this.getTag(sender.getItemInHand(), true);
            tag.set("powerToolCommands", new NBTTagList());
            context.sendMessage("basics", "&aRemoved all commands bound to this item!");
            String nameToRemove = BukkitUtils.getItemStackName(sender.getItemInHand());
            if (nameToRemove == null || nameToRemove.startsWith("§cPowerTool:"))
            {
                BukkitUtils.renameItemStack(sender.getItemInHand(), true, (String[]) null);
            }
        }
    }

    @Alias(names = "ptr")
    @Command(desc = "Removes a command from your powertool", flags = @Flag(longName = "chat", name = "c"), usage = "[command] [-chat]")
    public void remove(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eNo more power for you!");
        if (context.hasIndexed(0))
        {
            String cmd = context.getStrings(0);
            if (context.hasFlag("c"))
            {
                cmd = "chat:" + cmd;
            }
            NBTTagCompound tag = this.getTag(sender.getItemInHand(), true);
            NBTTagList ptVals = (NBTTagList)tag.get("powerToolCommands");
            if (ptVals != null || ptVals.size() == 0)
            {
                NBTTagList newVals = new NBTTagList();
                for (int i = 0; i < ptVals.size(); i++)
                {
                    if (((NBTTagString)ptVals.get(i)).data.equalsIgnoreCase(cmd))
                    {
                        context.sendMessage("basics", "&aRemoved the command: &e%s &abound to this item!", cmd);
                    }
                    else
                    {
                        newVals.add(ptVals.get(i));
                    }
                }
                tag.set("powerToolCommands", newVals);
                this.printList(sender, newVals, false);
                this.rename(sender, newVals);
            }
        }
        else
        {
            NBTTagCompound tag = this.getTag(sender.getItemInHand(), true);
            NBTTagList ptVals = (NBTTagList)tag.get("powerToolCommands");
            if (ptVals != null || ptVals.size() == 0)
            {
                NBTTagList newVals = new NBTTagList();
                for (int i = 0; i < ptVals.size() - 1; i++)
                {
                    newVals.add(ptVals.get(i));
                }
                tag.set("powerToolCommands", newVals);
                context.sendMessage("basics", "&aRemoved the last command bound to this item!");
                this.printList(sender, newVals, false);
                this.rename(sender, newVals);
            }
        }
    }

    @Alias(names = "pta")
    @Command(desc = "Adds a command to your powertool", flags = {
        @Flag(longName = "chat", name = "c"),
        @Flag(longName = "replace", name = "r")
    }, usage = "<commandstring>", min = 1)
    public void add(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eYou already have enough power!");
        String cmd = context.getStrings(0);
        if (sender.getItemInHand().getType().equals(Material.AIR))
        {
            blockCommand(context, "basics", "&eYou do not have an item in your hand to bind the command to!");
        }
        if (context.hasFlag("c"))
        {
            cmd = "chat:" + cmd;
        }
        this.addPowerTool(sender, cmd, !context.hasFlag("r"));
    }

    @Alias(names = "ptl")
    @Command(desc = "Lists your powertool-bindings.", flags = @Flag(longName = "all", name = "a"))
    public void list(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eYou already have enough power!");
        if (context.hasFlag("a"))
        {
            for (ItemStack item : sender.getInventory().getContents())
            {
                NBTTagCompound tag = this.getTag(item, true);
                if (tag == null)
                {
                    continue;
                }
                NBTTagList list = (NBTTagList)tag.get("powerToolCommands");
                if (list == null || list.size() == 0)
                {
                    continue;
                }
                String itemName = BukkitUtils.getItemStackName(item);
                if (itemName == null)
                {
                    sender.sendMessage("&6" + MaterialMatcher.get().getNameFor(item) + "&6:");
                }
                else
                {
                    sender.sendMessage("&6" + itemName + "&6:");
                }
                this.printList(sender, list, false);
            }
        }
        else
        {
            if (sender.getItemInHand().getType().equals(Material.AIR))
            {
                blockCommand(context, "basics", "&eYou do not have an item in your hand.");
            }

            NBTTagCompound tag = this.getTag(sender.getItemInHand(), true);
            this.printList(sender, (NBTTagList) tag.get("powerToolCommands"), false);
        }
    }

    private NBTTagCompound getTag(ItemStack item, boolean create)
    {
        if (item == null)
        {
            return null;
        }
        CraftItemStack cis = (CraftItemStack)item;
        NBTTagCompound tag = cis.getHandle().getTag();
        if (tag == null && create)
        {
            cis.getHandle().setTag(tag = new NBTTagCompound());
        }
        return tag;
    }

    private void printList(User user, NBTTagList ptVals, boolean lastAsNew)
    {
        if (ptVals == null || ptVals.size() == 0)
        {
            user.sendMessage("basics", "&cNo commands saved on this item!");
            return;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (; i < ptVals.size() - 1; i++)
        {
            sb.append("\n&f").append(((NBTTagString)ptVals.get(i)).data);
        }
        if (lastAsNew)
        {
            user.sendMessage("basics", "&6%d &ecommand(s) bound to this item:%s\n&aNew: &e%s", i + 1, sb.toString(), ((NBTTagString)ptVals.get(i)).data);
        }
        else
        {
            user.sendMessage("basics", "&6%d &ecommand(s) bound to this item:%s\n&f%s", i + 1, sb.toString(), ((NBTTagString)ptVals.get(i)).data);
        }
        sb.append("\n&f").append(((NBTTagString) ptVals.get(i)).data);

    }

    private void rename(User user, NBTTagList ptVals)
    {

        List<String> list = BukkitUtils.getItemStackLore(user.getItemInHand());
        if (list == null || list.isEmpty() || list.get(0).startsWith("§cPowerTool"))
        {
            if (ptVals == null || ptVals.size() == 0)
            {
                BukkitUtils.renameItemStack(user.getItemInHand(), true, (String[]) null);
            }
            else
            {
                list = new ArrayList<String>();
                list.add("&cPowerTool");
                for (int j = 0; j < ptVals.size(); j++)
                {
                    list.add(((NBTTagString) ptVals.get(j)).data);
                }
                BukkitUtils.renameItemStack(user.getItemInHand(), true, list.toArray(new String[list.size()]));
            }
        }

    }

    private void addPowerTool(User user, String command, boolean add)
    {
        NBTTagCompound tag = this.getTag(user.getItemInHand(), true);
        NBTTagList ptVals;
        if (add)
        {
            ptVals = (NBTTagList)tag.get("powerToolCommands");
            if (ptVals == null)
            {
                tag.set("powerToolCommands", ptVals = new NBTTagList());
            }
        }
        else
        {
            tag.set("powerToolCommands", ptVals = new NBTTagList());
        }
        ptVals.add(new NBTTagString(command, command));
        this.printList(user, ptVals, true);
        this.rename(user, ptVals);
    }
}
