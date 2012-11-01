package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.user.User;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

/**
 * A Kit of Items a User can receive
 */
public class Kit
{
    private ItemStack[] items;
    private boolean giveKitOnFirstJoin;
    private int limitUsagePerPlayer;
    private Permission permission;
    private String name;

    //TODO register permissions
    public Kit(String name, Collection<ItemStack> items)
    {
        this(name, (ItemStack[])items.toArray());
    }

    public Kit(final String name, ItemStack... items)
    {
        this.items = items;
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

    public boolean give(CommandSender sender, User user)
    {
        //TODO get kits only x-time
        //TODO delay between kits
        //TODO give kit to all players online
        //TODO starterKit on login (not here)
        if (!this.getPermission().isAuthorized(sender))
        {
            denyAccess(sender, "basics", "You are not allowed to give this kit.");
        }
        //TODO util for that could use it more often for other modules
        PlayerInventory inventory = user.getInventory();
        ItemStack[] oldInventory = inventory.getContents();
        Map map = inventory.addItem(items);
        if (!map.isEmpty())
        {
            user.getInventory().clear();
            user.getInventory().addItem(oldInventory);
            return false;
        }
        return true;
    }
    //TODO converter and add default to config to config and back

    public Permission getPermission()
    {
        return this.permission;
    }
}
