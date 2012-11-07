package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.blockCommand;

/**
 * The powertool command allows binding commands/chatmakros to a specific item
 * using NBT-Data.
 * /powertool
 */
public class PowerToolCommand
{
    @Command(
        names = { "pt", "powertool" },
        desc = "Binds a command to the item in hand.",
        usage = "<command> [arguments] [-add][-rem][-rema][-c][-l][-la][-ren]",
        flags =
        {
            @Flag(longName = "append", name = "add"), 
            @Flag(longName = "remove", name = "rem"),
            @Flag(longName = "removeall", name = "rema"),
            @Flag(longName = "chat", name = "c"),
            @Flag(longName = "list", name = "l"),
            @Flag(longName = "listall", name = "la"),
            @Flag(longName = "rename", name = "ren")
        })
    public void powertool(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eYou already have enough power!");
        if (context.hasFlag("la"))
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
                    sender.sendMessage("&6"+MaterialMatcher.get().getNameFor(item) + "&6:");
                }
                else
                {
                    sender.sendMessage("&6"+itemName + "&6:");
                }
                this.printList(sender, list, false, false);
            }
            return;
        }
        if (context.hasFlag("rema"))
        {
            for (ItemStack item : sender.getInventory().getContents())
            {
                NBTTagCompound tag = this.getTag(item, false);
                if (tag == null)
                {
                    continue;
                }
                tag.set("powerToolCommands", new NBTTagList());
                String nameToRemove = BukkitUtils.getItemStackName(item);
                if (nameToRemove == null || nameToRemove.startsWith("§cPT"))
                {
                    BukkitUtils.renameItemStack(sender.getItemInHand(), null);
                }
            }
            context.sendMessage("basics", "&aRemoved all commands bound to item in your inventory!");
            return;
        }
        if (sender.getItemInHand().getType().equals(Material.AIR))
        {
            blockCommand(context, "basics", "&eYou do not have an item in your hand to bind the command to!");
        }
        if (context.hasFlag("l"))
        {
            NBTTagCompound tag = this.getTag(sender.getItemInHand(), true);
            this.printList(sender, (NBTTagList)tag.get("powerToolCommands"), false, false);
            return;
        }    
        if (context.hasIndexed(0))
        {
            String cmd = context.getStrings(0);
            if (context.hasFlag("c"))
            {
                cmd = "chat:" + cmd;
            }
            if (context.hasFlag("rem"))
            {
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
                    this.printList(sender, ptVals, false, context.hasFlag("ren"));
                }
            }
            else
            {
                this.addPowerTool(sender, cmd, context.hasFlag("add"),context.hasFlag("ren"));
            }
        }
        else
        {
            if (context.hasFlag("rem"))
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
                    this.printList(sender, ptVals, false, context.hasFlag("ren"));
                }
            }
            else
            {
                NBTTagCompound tag = this.getTag(sender.getItemInHand(), true);
                tag.set("powerToolCommands", new NBTTagList());
                context.sendMessage("basics", "&aRemoved all commands bound to this item!");
                String nameToRemove = BukkitUtils.getItemStackName(sender.getItemInHand());
                if (nameToRemove == null || nameToRemove.startsWith("§cPT"))
                {
                    BukkitUtils.renameItemStack(sender.getItemInHand(), null);
                }
            }
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
    
    private void printList(User user, NBTTagList ptVals, boolean lastAsNew, boolean rename)
    {
        if (ptVals == null || ptVals.size() == 0)
        {
            user.sendMessage("basics", "&cNo commands saved on this item!");
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
        sb.append("\n&f").append(((NBTTagString)ptVals.get(i)).data);
        if (rename)
        {
            String name = BukkitUtils.getItemStackName(user.getItemInHand());
            if (name == null || name.startsWith("§cPT"))
            {
                if (ptVals == null || ptVals.size() == 0)
                {
                    BukkitUtils.renameItemStack(user.getItemInHand(), null);
                }
                else
                {
                    BukkitUtils.renameItemStack(user.getItemInHand(), "&cPT" + sb.toString().replaceAll("\n&f", "&c : &e"));
                }
            }
        }
    }

    private void addPowerTool(User user, String command, boolean add, boolean rename)
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
        this.printList(user, ptVals, true, rename);
    }
}
