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
import net.minecraft.server.v1_4_5.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * The powertool command allows binding commands/chatmacros to a specific item
 * using NBT-Data. /powertool
 */
public class PowerToolCommand extends ContainerCommand
{

    public PowerToolCommand(Module module)
    {
        super(module, "pt", "Binding shortcuts to an item.", "powertool");
    }

    @Override
    public void run(CommandContext context)
    {
        if (context.hasIndexed(0))
        {
            //TODO change context to have the replace flag set
            this.add(context);
            return;
        }
        super.run(context);
    }

    @Alias(names = "ptc")
    @Command(
    desc = "Removes all command from your powertool",
    flags =
    @Flag(longName = "all", name = "a"), usage = "[-a]")
    public void clear(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eNo more power for you!");
        if (context.hasFlag("a"))
        {
            boolean cleared = false;
            for (ItemStack item : sender.getInventory().getContents())
            {
                if (this.clear(context, item, true))
                {
                    cleared = true;
                }
            }
            if (cleared)
            {
                context.sendMessage("basics", "&aRemoved all commands bound to items in your inventory!");
            }
            else
            {

                context.sendMessage("basics", "&eThere are no commands bounds to items in your inventory!");
            }
        }
        else
        {
            if (sender.getItemInHand().getTypeId() == 0)
            {
                context.sendMessage("basics", "&eYou are not holding any item in your hand.");
                return;
            }
            if (this.clear(context, sender.getItemInHand(), false))
            {
                context.sendMessage("basics", "&aRemoved all commands bound to this item!");
            }
        }
    }

    private boolean clear(CommandContext context, ItemStack item, boolean noMessage)
    {
        NBTTagCompound tag = this.getTag(item, false);
        if (!checkForTag(context, item, noMessage))
        {
            return false;
        }
        tag.set("powerToolCommands", new NBTTagList());
        this.removeLore(item);
        return true;
    }

    private boolean checkForTag(CommandContext context, ItemStack item, boolean noMessage)
    {
        return this.checkForTag(context, this.getTag(item, false), noMessage);
    }

    private boolean checkForTag(CommandContext context, NBTTagCompound tag, boolean noMessage)
    {
        if (tag == null || tag.getList("powerToolCommands") == null || tag.getList("powerToolCommands").size() == 0)
        {
            if (!noMessage)
            {
                context.sendMessage("basics", "&cNo commands saved on this item!");
            }
            return false;
        }
        return true;
    }

    private void removeLore(ItemStack item)
    {
        String nameToRemove = item.getItemMeta().getLore().get(0);
        if (nameToRemove == null || nameToRemove.startsWith("§cPowertool:"))
        {
            ItemMeta meta = item.getItemMeta();
            meta.setLore(new ArrayList<String>());
            item.setItemMeta(meta);
        }
    }

    @Alias(names = "ptr")
    @Command(
    names =
    {
        "remove", "del", "delete", "rm"
    },
    desc = "Removes a command from your powertool",
    flags =
    @Flag(longName = "chat", name = "c"), usage = "[command] [-chat]")
    public void remove(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eNo more power for you!");
        if (sender.getItemInHand().getTypeId() == 0)
        {
            context.sendMessage("basics", "&eYou are not holding any item in your hand.");
            return;
        }
        if (context.hasIndexed(0))
        {
            String cmd = context.getStrings(0);
            if (context.hasFlag("c"))
            {
                cmd = "chat:" + cmd;
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
        NBTTagCompound tag = this.getTag(item, true);
        if (!this.checkForTag(context, tag, false))
        {
            return;
        }
        NBTTagList ptVals = (NBTTagList) tag.get("powerToolCommands");
        NBTTagList newVals = new NBTTagList();
        boolean removed = false;
        for (int i = 0; i < ptVals.size() - 1; i++)
        {
            if (cmd == null)
            {
                newVals.add(ptVals.get(i));
            }
            else
            {
                if (((NBTTagString) ptVals.get(i)).data.equalsIgnoreCase(cmd))
                {
                    removed = true;
                }
                else
                {
                    newVals.add(ptVals.get(i));
                }
            }
        }
        if (cmd == null)
        {
            context.sendMessage("basics", "&aRemoved the last command bound to this item!");
        }
        else
        {
            if (removed)
            {
                context.sendMessage("basics", "&aRemoved the command: &e%s &abound to this item!", cmd);
            }
            else
            {
                context.sendMessage("basics", "&cThe command &e%s &cwas not found on this item!", cmd);
                return;
            }
        }
        if (newVals.size() == 0)
        {
            tag.set("powerToolCommands",new NBTTagList());
            context.sendMessage("basics", "&eNo more commands saved on this item!");
        }
        else
        {
            tag.set("powerToolCommands", newVals);
            this.printList(context, newVals, false);
        }
        this.rename(item, newVals);
    }

    @Alias(names = "pta")
    @Command(
    desc = "Adds a command to your powertool",
    flags =
    {
        @Flag(longName = "chat", name = "c"),
        @Flag(longName = "replace", name = "r")
    },
    usage = "<commandstring>", min = 1)
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
        this.addPowerTool(context, sender, cmd, !context.hasFlag("r"));
    }

    private void addPowerTool(CommandContext context, User user, String command, boolean add)
    {
        NBTTagCompound tag = this.getTag(user.getItemInHand(), true);
        NBTTagList ptVals;
        if (add)
        {
            ptVals = (NBTTagList) tag.get("powerToolCommands");
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
        this.printList(context, ptVals, true);
        this.rename(user.getItemInHand(), ptVals);
    }

    @Alias(names = "ptl")
    @Command(desc = "Lists your powertool-bindings.",
    flags =
    @Flag(longName = "all", name = "a"))
    public void list(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eYou already have enough power!");
        if (context.hasFlag("a"))
        {
            for (ItemStack item : sender.getInventory().getContents())
            {
                NBTTagCompound tag = this.getTag(item, true);
                if (this.checkForTag(context, tag, true))
                {
                    continue;
                }
                String itemName = item.getItemMeta().getDisplayName();
                if (itemName == null)
                {
                    sender.sendMessage("&6" + MaterialMatcher.get().getNameFor(item) + "&6:");
                }
                else
                {
                    sender.sendMessage("&6" + itemName + "&6:");
                }
                this.printList(context, tag.getList("powerToolCommands"), false);
            }
        }
        else
        {
            if (sender.getItemInHand().getType().equals(Material.AIR))
            {
                blockCommand(context, "basics", "&eYou do not have an item in your hand.");
            }
            NBTTagCompound tag = this.getTag(sender.getItemInHand(), true);
            if (this.checkForTag(context, tag, false))
            {
                return;
            }
            this.printList(context, tag.getList("powerToolCommands"), false);
        }
    }

    private NBTTagCompound getTag(ItemStack item, boolean create)
    {
        if (item == null)
        {
            return null;
        }
        //TODO fix it :(
        /*
        CraftItemStack cis = (CraftItemStack) item;
        if (cis.getHandle() == null)
        {
            return null;
        }
        NBTTagCompound tag = cis.getHandle().getTag();
        if (tag == null && create)
        {
            cis.getHandle().setTag(tag = new NBTTagCompound());
        }
        return tag;
        */
        return null;
    }

    private void printList(CommandContext context, NBTTagList ptVals, boolean lastAsNew)
    {
        if (ptVals == null || ptVals.size() == 0)
        {
            context.sendMessage("basics", "&cNo commands saved on this item!");
            return;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (; i < ptVals.size() - 1; i++)
        {
            sb.append("\n&f").append(((NBTTagString) ptVals.get(i)).data);
        }
        if (lastAsNew)
        {
            context.sendMessage("basics", "&6%d &ecommand(s) bound to this item:%s\n&e%s &6(&aNEW&6)", i + 1, sb.toString(), ((NBTTagString) ptVals.get(i)).data);
        }
        else
        {
            context.sendMessage("basics", "&6%d &ecommand(s) bound to this item:%s\n&f%s", i + 1, sb.toString(), ((NBTTagString) ptVals.get(i)).data);
        }
        sb.append("\n&f").append(((NBTTagString) ptVals.get(i)).data);
    }

    private void rename(ItemStack item, NBTTagList ptVals)
    {
        List<String> list = item.getItemMeta().getLore();
        if (list == null || list.isEmpty() || list.get(0).startsWith("§cPowerTool"))
        {
            if (ptVals == null || ptVals.size() == 0)
            {
                ItemMeta meta = item.getItemMeta();
                meta.setLore(new ArrayList<String>());
                item.setItemMeta(meta);
            }
            else
            {
                list = new ArrayList<String>();
                list.add("&cPowerTool");
                for (int j = 0; j < ptVals.size(); j++)
                {
                    list.add(((NBTTagString) ptVals.get(j)).data);
                }
                ItemMeta meta = item.getItemMeta();
                meta.setLore(list);
                item.setItemMeta(meta);
            }
        }
    }
}
