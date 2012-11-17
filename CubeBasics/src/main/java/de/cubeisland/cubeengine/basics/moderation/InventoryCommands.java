package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.inventory.ItemStack;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

/**
 * Contains commands that allow to modify an inventory.
 * /invsee
 * /clearinventory
 * /stash
 */
public class InventoryCommands
{
    private InvseeListener listener;
    private Basics basics;

    public InventoryCommands(Basics basics)
    {
        this.basics = basics;
        this.basics.registerListener(listener = new InvseeListener(basics));
    }

    @Command(desc = "Allows you to see into the inventory of someone else.", usage = "<player>", min = 1, max = 1)
    public void invsee(CommandContext context)
    {
        User sender = context.getSenderAsUser("bascics", "&cThis command can only be used by a player!");
        User user = context.getUser(0);
        if (user == null)
        {
            paramNotFound(context, "basics", "&cUser &2%s &cnot found!", context.getString(0));
        }
        boolean allowModify = false;
        if (BasicsPerm.COMMAND_INVSEE_MODIFY.isAuthorized(sender))
        {
            allowModify = true;
        }
        if (BasicsPerm.COMMAND_INVSEE_PREVENTMODIFY.isAuthorized(user))
        {
            allowModify = false;
        }
        if (BasicsPerm.COMMAND_INVSEE_NOTIFY.isAuthorized(user))
        {
            user.sendMessage("basics", "&2%s &eis looking into your inventory.", sender.getName());
        }
        sender.openInventory(user.getInventory());
        listener.addInventory(sender, allowModify);
    }

    @Command(desc = "Stashes or unstashes your inventory to reuse later", max = 0)
    public void stash(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&cThis command can only be used by a player!"); //TODO funny message for console :)
        ItemStack[] stashedInv = sender.getAttribute(basics, "stash_Inventory");
        ItemStack[] stashedArmor = sender.getAttribute(basics, "stash_Armor");
        ItemStack[] InvToStash = sender.getInventory().getContents().clone();
        ItemStack[] ArmorToStash = sender.getInventory().getArmorContents().clone();
        if (stashedInv != null)
        {
            sender.getInventory().setContents(stashedInv);
        }
        else
        {
            sender.getInventory().clear();
        }
        sender.setAttribute(basics, "stash_Inventory", InvToStash);
        if (stashedArmor != null)
        {
            sender.getInventory().setBoots(stashedArmor[0]);
            sender.getInventory().setLeggings(stashedArmor[1]);
            sender.getInventory().setChestplate(stashedArmor[2]);
            sender.getInventory().setHelmet(stashedArmor[3]);
        }
        else
        {
            sender.getInventory().setBoots(null);
            sender.getInventory().setLeggings(null);
            sender.getInventory().setChestplate(null);
            sender.getInventory().setHelmet(null);
        }
        sender.setAttribute(basics, "stash_Armor", ArmorToStash);
        sender.sendMessage("basics", "&aSwapped stashed Inventory!");
    }

    @Command(names = {
        "clearinventory", "ci", "clear"
    }, desc = "Clears the inventory", usage = "[player]", flags = {
        @Flag(longName = "removeArmor", name = "ra")
    }, max = 1)
    public void clearinventory(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        User user = sender;
        boolean other = false;
        if (context.hasIndexed(0))
        {
            user = context.getUser(0);
            if (user == null)
            {
                invalidUsage(context, "core", "&cUser &2%s &cnot found!", context.getString(0));
            }
            other = true;
        }
        if (other && !BasicsPerm.COMMAND_CLEARINVENTORY_OTHER.isAuthorized(context.getSender()))
        {
            denyAccess(context, "basics", "&cYou are not allowed to clear the inventory of other users!");
        }
        user.getInventory().clear();
        if (context.hasFlag("ra"))
        {
            user.getInventory().setBoots(null);
            user.getInventory().setLeggings(null);
            user.getInventory().setChestplate(null);
            user.getInventory().setHelmet(null);
        }
        user.updateInventory();
        user.sendMessage("basics", "&eCleared inventory!");
        if (other)
        {
            sender.sendMessage("basics", "&aCleared Inventory of &2%s&a!", user.getName());
        }
    }
}
