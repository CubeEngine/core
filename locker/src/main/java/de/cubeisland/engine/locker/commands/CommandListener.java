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
package de.cubeisland.engine.locker.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Triplet;
import de.cubeisland.engine.locker.storage.Lock;
import de.cubeisland.engine.locker.storage.LockManager;
import de.cubeisland.engine.locker.storage.LockType;
import de.cubeisland.engine.locker.storage.ProtectionFlag;

import static de.cubeisland.engine.locker.commands.CommandListener.CommandType.*;
import static de.cubeisland.engine.locker.commands.LockerCommands.isNotUser;
import static de.cubeisland.engine.locker.storage.LockType.*;

public class CommandListener implements Listener
{
    private Map<String, Triplet<CommandType, String, Boolean>> map = new HashMap<>();
    private Map<String,Long> persist = new HashMap<>();

    private de.cubeisland.engine.locker.Locker module;
    private LockManager manager;

    public CommandListener(de.cubeisland.engine.locker.Locker module, LockManager manager)
    {
        this.module = module;
        this.manager = manager;
    }

    public void setCommandType(CommandSender sender, CommandType commandType, String s, boolean b)
    {
        this.setCommandType0(sender, commandType, s, b);
    }

    public void setCommandType(CommandSender sender, CommandType commandType, String s)
    {
        this.setCommandType0(sender, commandType, s, false);
    }

    private void setCommandType0(CommandSender sender, CommandType commandType, String s, boolean b)
    {
        if (isNotUser(sender)) return;
        map.put(sender.getName(), new Triplet<>(commandType, s, b));
        if (this.doesPersist(sender.getName()))
        {
            sender.sendTranslated("&ePersist mode is active. Your command will be repeated until reusing &6/cpersist");
        }
    }

    private boolean doesPersist(String name)
    {
        Long lastUsage = this.persist.get(name);
        return lastUsage != null && (System.currentTimeMillis() - lastUsage) < TimeUnit.MINUTES.toMillis(5);
    }

    /**
     * Toggles the persist mode for given user
     *
     * @param sender
     * @return true if persist mode is on for given user
     */
    public boolean persist(User sender)
    {
        if (doesPersist(sender.getName()))
        {
            persist.remove(sender.getName());
            this.map.remove(sender.getName());
            return false;
        }
        persist.put(sender.getName(), System.currentTimeMillis());
        return true;
    }

    @EventHandler
    public void onRightClickBlock(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.PHYSICAL
            || event.getAction() == Action.LEFT_CLICK_AIR
            || event.getAction() == Action.RIGHT_CLICK_AIR
            || event.getPlayer().isSneaking())
        {
            return;
        }
        if (!map.keySet().contains(event.getPlayer().getName())) return;
        if (event.getClickedBlock() != null)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            Location location = event.getClickedBlock().getLocation();
            Triplet<CommandType, String, Boolean> triplet = map.get(user.getName());
            Lock lock = this.manager.getLockAtLocation(location, user, triplet.getFirst() != INFO);
            if (this.handleInteract1(triplet, lock, user, location.getBlock().getState() instanceof InventoryHolder,
                     this.manager.canProtect(event.getClickedBlock().getType()), event))
            {
                return;
            }

            switch (triplet.getFirst())
            {
            case C_PRIVATE:
                this.manager.createLock(event.getClickedBlock().getType(), location, user, C_PRIVATE.lockType, triplet.getSecond(), triplet.getThird());
                break;
            case C_PUBLIC:
                this.manager.createLock(event.getClickedBlock().getType(), location, user, C_PUBLIC.lockType, triplet.getSecond(), false);
                break;
            case C_DONATION:
                this.manager.createLock(event.getClickedBlock().getType(), location, user, C_DONATION.lockType, triplet.getSecond(), triplet.getThird());
                break;
            case C_FREE:
                this.manager.createLock(event.getClickedBlock().getType(), location, user, C_FREE.lockType, triplet.getSecond(), triplet.getThird());
                break;
            case C_GUARDED:
                this.manager.createLock(event.getClickedBlock().getType(), location, user, C_GUARDED.lockType, triplet.getSecond(), triplet.getThird());
                break;
            default:
                this.handleInteract2(triplet.getFirst(), lock, user, triplet.getSecond(), triplet.getThird(), location,
                     event.getClickedBlock().getState() instanceof InventoryHolder ? (InventoryHolder)event.getClickedBlock().getState() : null);
            }
            this.cmdUsed(user);
            event.setCancelled(true);
        }
    }

    private void cmdUsed(User user)
    {
        if (doesPersist(user.getName()))
        {
            this.persist.put(user.getName(), System.currentTimeMillis());
        }
        else
        {
            this.map.remove(user.getName());
        }
    }

    private boolean handleInteract1(Triplet<CommandType, String, Boolean> triplet, Lock lock, User user, boolean isHolder, boolean canProtect, Cancellable event)
    {
        if (triplet.getFirst().isCreator())
        {
            if (lock != null)
            {
                user.sendTranslated("&eThere is already a protection here!");
                this.cmdUsed(user);
                event.setCancelled(true);
                return true;
            }
            else
            {
                if (!isHolder)
                {
                    switch (triplet.getFirst())
                    {
                    case C_DONATION:
                    case C_FREE:
                    case C_GUARDED:
                        user.sendTranslated("&eYou can only apply guarded, donation and free protections to inventory-holders!");
                        event.setCancelled(true);
                        this.cmdUsed(user);
                        return true;
                    }
                }
                if (!canProtect)
                {
                    this.cmdUsed(user);
                    user.sendTranslated("&cYou cannot protect this!");
                    event.setCancelled(true);
                    return true; // do nothing entity is not protectable
                }
            }
        }
        else if (lock == null)
        {
            user.sendTranslated("&6No protection detected here!");
            event.setCancelled(true);
            this.cmdUsed(user);
            return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClickEntity(PlayerInteractEntityEvent event)
    {
        if (event.getPlayer().isSneaking()) return;
        if (!map.keySet().contains(event.getPlayer().getName())) return;
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        try
        {
            Location location = event.getRightClicked().getLocation();
            Triplet<CommandType, String, Boolean> triplet = map.get(user.getName());
            Lock lock = this.manager.getLockForEntityUID(event.getRightClicked().getUniqueId(), triplet.getFirst() != INFO);
            if (this.handleInteract1(triplet, lock, user, event.getRightClicked() instanceof InventoryHolder,
                                     this.manager.canProtect(event.getRightClicked().getType()), event))
            {
                return;
            }
            switch (triplet.getFirst())
            {
            case C_PRIVATE:
                this.manager.createLock(event.getRightClicked(), user, C_PRIVATE.lockType, triplet.getSecond(), triplet
                    .getThird());
                break;
            case C_PUBLIC:
                this.manager.createLock(event.getRightClicked(), user, C_PUBLIC.lockType, triplet.getSecond(), triplet
                    .getThird());
                break;
            case C_DONATION:
                this.manager.createLock(event.getRightClicked(), user, C_DONATION.lockType, triplet.getSecond(), triplet
                    .getThird());
                break;
            case C_FREE:
                this.manager.createLock(event.getRightClicked(), user, C_FREE.lockType, triplet.getSecond(), triplet
                    .getThird());
                break;
            case C_GUARDED:
                this.manager.createLock(event.getRightClicked(), user, C_GUARDED.lockType, triplet.getSecond(), triplet
                    .getThird());
                break;
            default:
                this.handleInteract2(triplet.getFirst(), lock, user, triplet.getSecond(), triplet.getThird(), location,
                                     event.getRightClicked() instanceof InventoryHolder ? (InventoryHolder)event.getRightClicked() : null);
            }
            this.cmdUsed(user);
            event.setCancelled(true);
        }
        catch (Exception ex)
        {
            this.module.getLog().error(ex, "Error with CommandInteract!");
            user.sendTranslated("&4An unknown error occurred!");
            user.sendTranslated("&4Please report this error to an administrator.");
        }
    }

    private void handleInteract2(CommandType first, Lock lock, User user, String second, Boolean third, Location location, InventoryHolder possibleHolder)
    {
        switch (first)
        {
        case INFO:
            lock.showInfo(user);
            break;
        case MODIFY:
            lock.modifyLock(user, second);
            break;
        case REMOVE:
            this.manager.removeLock(lock, user, false);
            break;
        case UNLOCK:
            lock.unlock(user, location, second);
            break;
        case INVALIDATE_KEYS:
            if (!lock.isOwner(user))
            {
                user.sendTranslated("&cThis is not your protection!");
            }
            else if (lock.hasPass())
            {
                user.sendTranslated("&eYou cannot invalidate KeyBooks for password protected locks. &aChange the password to invalidate them!");
            }
            else
            {
                lock.invalidateKeyBooks();
                if (possibleHolder != null)
                {
                    for (HumanEntity viewer : possibleHolder.getInventory().getViewers())
                    {
                        viewer.closeInventory();
                    }
                }
            }
            break;
        case KEYS:
            if (lock.isOwner(user) || module.perms().CMD_KEY_OTHER.isAuthorized(user))
            {
                if (lock.isPublic())
                {
                    user.sendTranslated("&eThis protection is public!");
                }
                else
                {
                    lock.attemptCreatingKeyBook(user, third);
                }
            }
            else
            {
                user.sendTranslated("&cThis is not your protection!");
            }
            break;
        case GIVE:
            if (lock.isOwner(user) || module.perms().CMD_GIVE_OTHER.isAuthorized(user))
            {
                User newOwner = this.module.getCore().getUserManager().getExactUser(second);
                lock.setOwner(newOwner);
                user.sendTranslated("&2%s&e is now the owner of this protection.", newOwner.getName());
            }
            else
            {
                user.sendTranslated("&cThis is not your protection!");
            }
        case FLAGS_SET:
            if (lock.isOwner(user) || lock.hasAdmin(user) || module.perms().CMD_MODIFY_OTHER.isAuthorized(user))
            {
                short flags = 0;
                for (ProtectionFlag protectionFlag : ProtectionFlag.matchFlags(second))
                {
                    flags |= protectionFlag.flagValue;
                }
                lock.setFlags((short)(flags | lock.getFlags()));
                user.sendTranslated("&eFlags set!");
            }
            else
            {
                user.sendTranslated("&cYou are not allowed to modify this protections flags!");
            }
            break;
        case FLAGS_UNSET:
            if (lock.isOwner(user) || lock.hasAdmin(user) || module.perms().CMD_MODIFY_OTHER.isAuthorized(user))
            {
                if ("all".equalsIgnoreCase(second))
                {
                    lock.setFlags(ProtectionFlag.NONE);
                    user.sendTranslated("&aAll flags are now unset!");
                }
                else
                {
                    short flags = 0;
                    for (ProtectionFlag protectionFlag : ProtectionFlag.matchFlags(second))
                    {
                        flags |= protectionFlag.flagValue;
                    }
                    lock.setFlags((short)(lock.getFlags() & ~flags));
                    user.sendTranslated("&eFlags unset!");
                }
            }
            else
            {
                user.sendTranslated("&cYou are not allowed to modify this protections flags!");
            }
            break;
        default: throw new IllegalArgumentException();
        }

    }

    public enum CommandType
    {
        C_PRIVATE(PRIVATE),
        C_PUBLIC(PUBLIC),
        C_DONATION(DONATION),
        C_FREE(FREE),
        C_GUARDED(GUARDED),
        INFO,
        MODIFY,
        REMOVE,
        UNLOCK,
        INVALIDATE_KEYS,
        KEYS,
        GIVE,
        FLAGS_SET,
        FLAGS_UNSET;

        private CommandType(LockType lockType)
        {
            this.lockType = lockType;
        }

        private CommandType()
        {
            lockType = null;
        }

        public final LockType lockType;

        public boolean isCreator()
        {
            return lockType != null;
        }
    }
}
