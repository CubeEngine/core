/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.kits;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.CommandManager;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.InventoryUtil;
import org.joda.time.Duration;
import org.jooq.DSLContext;
import org.jooq.Record1;

/**
 * A Kit of Items a User can receive
 */
public class Kit
{
    private String name;
    private List<KitItem> items;

    public boolean isGiveKitOnFirstJoin()
    {
        return giveKitOnFirstJoin;
    }

    private boolean giveKitOnFirstJoin;
    private int limitUsagePerPlayer;
    private long limitUsageDelay;
    private Permission permission;
    private String customMessage;
    private List<String> commands;
    private DSLContext dsl;

    public Kit(Database db, final String name, boolean giveKitOnFirstJoin, int limitUsagePerPlayer, long limitUsageDelay, boolean usePermission, String customMessage, List<String> commands, List<KitItem> items)
    {
        this.dsl = db.getDSL();
        this.name = name;
        this.items = items;
        this.commands = commands;
        this.customMessage = customMessage;
        if (usePermission)
        {
            this.permission = KitsPerm.KITS.createChild(name);
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
        if (!force && this.getPermission() != null)
        {
            if (!this.getPermission().isAuthorized(sender))
            {
                sender.sendTranslated("You are not allowed to give this kit.");
                throw new PermissionDeniedException();
            }
        }
        if (!force)
        {
            if (limitUsagePerPlayer > 0)
            {
                Record1<Integer> record1 = this.dsl.select(TableKitsGiven.TABLE_KITS.AMOUNT).from(TableKitsGiven.TABLE_KITS).
                    where(TableKitsGiven.TABLE_KITS.KITNAME.like(this.name), TableKitsGiven.TABLE_KITS.USERID.eq(user.getEntity().getKey())).fetchOne();
                if (record1 != null && record1.value1() >= this.limitUsagePerPlayer)
                {
                    sender.sendTranslated("&cKit-limit reached.");
                    throw new PermissionDeniedException();
                }
            }
            if (limitUsageDelay != 0)
            {
                Long lastUsage = user.get(KitsAttachment.class).getKitUsage(this.name);
                if (lastUsage != null && System.currentTimeMillis() - lastUsage < limitUsageDelay)
                {
                    sender.sendTranslated("&eThis kit not available at the moment. &aTry again later!");
                    throw new PermissionDeniedException();
                }
            }
        }
        List<ItemStack> list = this.getItems();
        if (InventoryUtil.giveItemsToUser(user, list.toArray(new ItemStack[list.size()])))
        {
            this.dsl.insertInto(TableKitsGiven.TABLE_KITS, TableKitsGiven.TABLE_KITS.KITNAME, TableKitsGiven.TABLE_KITS.USERID, TableKitsGiven.TABLE_KITS.AMOUNT).values(this.getKitName(), user.getEntity().getKey(), 1)
                    .onDuplicateKeyUpdate().set(TableKitsGiven.TABLE_KITS.AMOUNT, TableKitsGiven.TABLE_KITS.AMOUNT.add(1)).execute();
            this.executeCommands(user);
            if (limitUsageDelay != 0)
            {
                user.get(KitsAttachment.class).setKitUsage(this.name);
            }
            return true;
        }
        return false;
    }

    private void executeCommands(User user)
    {
        if (this.commands != null && !this.commands.isEmpty())
        {
            CommandManager cm = user.getCore().getCommandManager();
            KitCommandSender kitCommandSender = new KitCommandSender(user);
            for (String cmd : commands)
            {
                cmd = cmd.replace("{PLAYER}", user.getName());
                cm.runCommand(kitCommandSender, cmd);
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
        List<ItemStack> list = new ArrayList<>();
        for (KitItem kitItem : this.items)
        {
            list.add(kitItem.getItemStack());
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
        private static final String NAME_PREFIX = "Kit | ";
        private final User user;

        public KitCommandSender(User user)
        {
            this.user = user;
        }

        public User getUser()
        {
            return this.user;
        }

        @Override
        public Core getCore()
        {
            return this.user.getCore();
        }

        @Override
        public boolean isAuthorized(Permission perm)
        {
            return this.hasPermission(perm.getName());
        }

        @Override
        public Locale getLocale()
        {
            return this.user.getLocale();
        }

        @Override
        public String translate(String message, Object... params)
        {
            return this.user.translate(message, params);
        }

        @Override
        public void sendTranslated(String message, Object... params)
        {
            this.user.sendTranslated(message, params);
        }

        @Override
        public void sendMessage(String string)
        {
            this.user.sendMessage(string);
        }

        @Override
        public void sendMessage(String[] strings)
        {
            this.user.sendMessage(strings);
        }

        @Override
        public Server getServer()
        {
            return this.user.getServer();
        }

        @Override
        public String getName()
        {
            return NAME_PREFIX + this.user.getName();
        }

        @Override
        public String getDisplayName()
        {
            return NAME_PREFIX + this.user.getDisplayName();
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
            return new HashSet<>();
        }

        @Override
        public boolean isOp()
        {
            return false;
        }

        @Override
        public void setOp(boolean bln)
        {}
    }
}
