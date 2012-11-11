package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.InventoryUtil;
import java.util.Collection;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.blockCommand;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

/**
 * A Kit of Items a User can receive
 */
public class Kit
{
    private static Basics basics = Basics.getInstance();
    //TODO command to create those
    private String name;
    private ItemStack[] items;
    private boolean giveKitOnFirstJoin;
    private int limitUsagePerPlayer; // TODO good way to do this?
    private long limitUsageDelay;
    private Permission permission;
    private String customMessage;

    // TODO ? add commands to execute to the kit (same with powertool)
    //e.g. /feed {PLAYER} | {PLAYER} will be replaced with the username that reveives the kit
    public Kit(String name, boolean giveKitOnFirstJoin, int limitUsagePerPlayer, long limitUsageDelay, boolean usePermission, String customMessage, Collection<ItemStack> items)
    {
        this(name, giveKitOnFirstJoin, limitUsagePerPlayer, limitUsageDelay, usePermission, customMessage, items.toArray(new ItemStack[0]));
    }

    public Kit(final String name, boolean giveKitOnFirstJoin, int limitUsagePerPlayer, long limitUsageDelay, boolean usePermission, String customMessage, ItemStack... items)
    {
        this.name = name;
        this.items = items;
        this.customMessage = customMessage;
        if (usePermission)
        {
            this.permission = new Permission()
            {
                private String permission = "cubeengine.basics.kits." + name.toLowerCase(Locale.ENGLISH);
                private PermissionDefault def = PermissionDefault.OP;

                public boolean isAuthorized(Permissible player)
                {
                    return player.hasPermission(permission);
                }

                public String getPermission()
                {
                    return this.permission;
                }

                public PermissionDefault getPermissionDefault()
                {
                    return this.def;
                }
            };
        }
        else
        {
            this.permission = null;
        }
        this.giveKitOnFirstJoin = giveKitOnFirstJoin;
        this.limitUsagePerPlayer = limitUsagePerPlayer;
        this.limitUsageDelay = limitUsageDelay;
    }

    public void give(CommandSender sender, User user, boolean force)
    {
        //TODO get kits only x-time
        //TODO give kit to all players online (not here)
        //TODO starterKit on login (not here)
        if (this.getPermission() != null)
        {
            if (!this.getPermission().isAuthorized(sender))
            {
                denyAccess(sender, "basics", "You are not allowed to give this kit.");
            }
        }
        if (limitUsageDelay != 0)
        {
            Long lastUsage = user.getAttribute(basics, "kitUsage_" + this.name);
            if (lastUsage != null)
            {
                if (System.currentTimeMillis() - lastUsage < limitUsageDelay)
                {
                    blockCommand(sender, "basisc", "This kit not availiable at the moment. Try again later!");
                }
            }
        }
        if (InventoryUtil.giveItemsToUser(user, items))
        {
            if (limitUsageDelay != 0)
            {
                user.setAttribute(basics, "kitUsage_" + this.name, System.currentTimeMillis());
            }
        }
        else
        {
            blockCommand(sender, "basics", "Not enough space for this kit!");
        }
    }

    public Permission getPermission()
    {
        return this.permission;
    }
    
    public String getCustomMessage()
    {
        return this.customMessage;
    }
}
