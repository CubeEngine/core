package de.cubeisland.cubeengine.basics.moderation.kit;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.InventoryUtil;
import de.cubeisland.cubeengine.core.util.time.Duration;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.blockCommand;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A Kit of Items a User can receive
 */
public class Kit
{
    private static Basics basics = Basics.getInstance();
    private String name;
    private List<KitItem> items;
    private boolean giveKitOnFirstJoin;
    private int limitUsagePerPlayer;
    private long limitUsageDelay;
    private Permission permission;
    private String customMessage;
    private List<String> commands;

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
                private PermDefault def = PermDefault.OP;

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
                public PermDefault getPermissionDefault()
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
        if (basics.getKitGivenManager().reachedUsageLimit(user, this.name, this.limitUsagePerPlayer))
        {
            denyAccess(sender, "basics", "&cKit-limit reached.");
        }
        //TODO check how many times user got his kit
        if (limitUsageDelay != 0)
        {
            Long lastUsage = user.getAttribute(basics, "kitUsage_" + this.name);
            if (lastUsage != null && System.currentTimeMillis() - lastUsage < limitUsageDelay)
            {
                blockCommand(sender, "basisc", "&eThis kit not availiable at the moment. &aTry again later!");
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
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(kitItem.customName);
                item.setItemMeta(meta);
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
        config.usePerm = this.permission != null;
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
        {}

        @Override
        public void sendMessage(String[] strings)
        {}

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
        {}

        @Override
        public void recalculatePermissions()
        {}

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
        {}
    }
}
