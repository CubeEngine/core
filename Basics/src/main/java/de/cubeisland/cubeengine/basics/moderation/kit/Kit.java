package de.cubeisland.cubeengine.basics.moderation.kit;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.blockCommand;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.InventoryUtil;
import de.cubeisland.cubeengine.core.util.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

/**
 * A Kit of Items a User can receive
 */
public class Kit
{
    private static Basics basics = Basics.getInstance();
    //TODO command to create those
    private String name;
    private List<KitItem> items;
    private boolean giveKitOnFirstJoin;
    private int limitUsagePerPlayer; // TODO will need its own db-table for this
    private long limitUsageDelay;
    private Permission permission;
    private String customMessage;
    private List<String> commands;

    // TODO ? add commands to execute to the kit (same with powertool)
    //e.g. /feed {PLAYER} | {PLAYER} will be replaced with the username that reveives the kit
    public Kit(final String name, boolean giveKitOnFirstJoin, int limitUsagePerPlayer, long limitUsageDelay, boolean usePermission, String customMessage, List<String> commands, List<KitItem> items)
    {

        this.name = name;
        this.items = items;
        this.commands = commands;
        this.customMessage = customMessage;
        if (usePermission)
        {
            this.permission = new Permission()
            {
                private String permission = "cubeengine.basics.kits." + name.toLowerCase(Locale.ENGLISH);
                private PermissionDefault def = PermissionDefault.OP;

                @Override
                public boolean isAuthorized(Permissible player)
                {
                    return player.hasPermission(permission);
                }

                @Override
                public String getPermission()
                {
                    return this.permission;
                }

                @Override
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

    public boolean give(CommandSender sender, User user, boolean force)
    {
        //TODO give kit to all players online (not here)
        //TODO starterKit on login (not here)
        if (!force && this.getPermission() != null)
        {
            if (!this.getPermission().isAuthorized(sender))
            {
                denyAccess(sender, "basics", "You are not allowed to give this kit.");
            }
        }
        //TODO check how many times user got his kit
        if (limitUsageDelay != 0)
        {
            Long lastUsage = user.getAttribute(basics, "kitUsage_" + this.name);
            if (lastUsage != null && System.currentTimeMillis() - lastUsage < limitUsageDelay)
            {
                blockCommand(sender, "basisc", "This kit not availiable at the moment. Try again later!");
            }
        }
        List<ItemStack> list = this.getItems();
        if (InventoryUtil.giveItemsToUser(user, list.toArray(new ItemStack[list.size()])))
        {
            this.executeCommands(user);
            if (limitUsageDelay != 0)
            {
                user.setAttribute(basics, "kitUsage_" + this.name, System.currentTimeMillis());
            }
            return true;
        }
        return false;
    }

    private void executeCommands(User user)
    {
        if (this.commands != null && !this.commands.isEmpty())
        {
            for (String cmd : commands)
            {
                cmd = cmd.replace("{PLAYER}", user.getName());
                Bukkit.dispatchCommand(KitCommandSender.instance, cmd);
            }
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

    private List<ItemStack> getItems()
    {
        List<ItemStack> list = new ArrayList<ItemStack>();
        for (KitItem kitItem : this.items)
        {
            ItemStack item = new ItemStack(kitItem.mat, kitItem.amount, kitItem.dura);
            if (kitItem.customName != null)
            {
                item = new CraftItemStack(item);
                BukkitUtils.renameItemStack(item, false, kitItem.customName);
            }
            list.add(item);
        }
        return list;
    }

    public void applyToConfig(KitConfiguration config)
    {
        config.customReceiveMsg = this.customMessage;
        config.giveOnFirstJoin = this.giveKitOnFirstJoin;
        config.kitCommands = this.commands;
        config.kitItems = this.items;
        config.kitName = this.name;
        config.limitUsage = this.limitUsagePerPlayer;
        config.limitUsageDelay = new Duration(this.limitUsageDelay);
        config.usePerm = this.permission == null ? false : true;
    }

    public String getKitName()
    {
        return this.name;
    }

    private static class KitCommandSender implements CommandSender
    {
        protected static KitCommandSender instance;

        static
        {
            instance = new KitCommandSender();
        }

        @Override
        public void sendMessage(String string)
        {
        }

        @Override
        public void sendMessage(String[] strings)
        {
        }

        @Override
        public Server getServer()
        {
            return Bukkit.getServer();
        }

        @Override
        public String getName()
        {
            return "~Server~";
        }

        @Override
        public boolean isPermissionSet(String string)
        {
            return true;
        }

        @Override
        public boolean isPermissionSet(org.bukkit.permissions.Permission prmsn)
        {
            return true;
        }

        @Override
        public boolean hasPermission(String string)
        {
            return true;
        }

        @Override
        public boolean hasPermission(org.bukkit.permissions.Permission prmsn)
        {
            return true;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln)
        {
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin)
        {
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i)
        {
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, int i)
        {
            return null;
        }

        @Override
        public void removeAttachment(PermissionAttachment pa)
        {
        }

        @Override
        public void recalculatePermissions()
        {
        }

        @Override
        public Set<PermissionAttachmentInfo> getEffectivePermissions()
        {
            return new HashSet<PermissionAttachmentInfo>();
        }

        @Override
        public boolean isOp()
        {
            return true;
        }

        @Override
        public void setOp(boolean bln)
        {
        }
    }
}
