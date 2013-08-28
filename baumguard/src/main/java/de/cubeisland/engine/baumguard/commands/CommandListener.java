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
package de.cubeisland.engine.baumguard.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.engine.baumguard.Baumguard;
import de.cubeisland.engine.baumguard.storage.Guard;
import de.cubeisland.engine.baumguard.storage.GuardManager;
import de.cubeisland.engine.baumguard.storage.GuardType;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Triplet;

import static de.cubeisland.engine.baumguard.commands.CommandListener.CommandType.*;
import static de.cubeisland.engine.baumguard.commands.GuardCommands.isNotUser;
import static de.cubeisland.engine.baumguard.storage.GuardType.*;

public class CommandListener implements Listener
{
    private Map<String, Triplet<CommandType, String, Boolean>> map = new HashMap<>();
    private Set<String> persist = new HashSet<>();

    private Baumguard module;
    private GuardManager manager;

    public CommandListener(Baumguard module, GuardManager manager)
    {
        this.module = module;
        this.manager = manager;
    }

    public void setCommandType(CommandSender sender, CommandType guardtype, String pass, boolean createKeyBook)
    {
        this.setCommandType0(sender, guardtype, pass, createKeyBook);
    }

    public void setCommandType(CommandSender sender, CommandType modify, String players)
    {
        this.setCommandType0(sender, modify, players, false);
    }

    private void setCommandType0(CommandSender sender, CommandType guardType, String s, boolean b)
    {
        if (isNotUser(sender)) return;
        map.put(sender.getName(), new Triplet<>(guardType, s, b));
        if (persist.contains(sender.getName()))
        {
            sender.sendTranslated("&ePersist mode is active. Your command will be repeated until reusing &6/cpersist");
        }
    }

    /**
     * Toggles the persist mode for given user
     *
     * @param sender
     * @return true if persist mode is on for given user
     */
    public boolean persist(User sender)
    {
        if (persist.contains(sender.getName()))
        {
            persist.remove(sender.getName());
            this.map.remove(sender.getName());
            return false;
        }
        persist.add(sender.getName());
        return true;
    }

    @EventHandler
    public void onRightClickBlock(PlayerInteractEvent event)
    {
        if (!map.keySet().contains(event.getPlayer().getName())) return;
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getClickedBlock() != null)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            Location location = event.getClickedBlock().getLocation();
            Triplet<CommandType, String, Boolean> triplet = map.get(user.getName());
            Guard guard = this.manager.getGuardAtLocation(location, triplet.getFirst() != INFO);
            if (triplet.getFirst().isCreator())
            {
                if (guard != null && !guard.isValidType())
                {
                    user.sendTranslated("&eExisting BlockProtection is not valid!");
                    guard.delete(user);
                    guard = null;
                }
                if (guard != null)
                {
                    user.sendTranslated("&eThis block is already protected!");
                    this.cmdUsed(user);
                    event.setCancelled(true);
                    return;
                }
                else
                {
                    if (!(location.getBlock().getState() instanceof InventoryHolder))
                    {
                        switch (triplet.getFirst())
                        {
                            case C_DONATION:
                            case C_FREE:
                            case C_GUARDED:
                                user.sendTranslated("&eYou can only apply guarded, donation and free protections to inventory-holders!");
                                event.setCancelled(true);
                                this.cmdUsed(user);
                                return;
                        }
                    }
                    if (!this.manager.canProtect(event.getClickedBlock().getType()))
                    {
                        this.cmdUsed(user);
                        user.sendTranslated("&cYou cannot protect this!");
                        return; // do nothing block is not protectable
                    }
                }
            }
            else if (guard == null)
            {
                user.sendTranslated("&6No protection detected here!");
                event.setCancelled(true);
                this.cmdUsed(user);
                return;
            }
            switch (triplet.getFirst())
            {
            case C_PRIVATE:
                this.manager.createGuard(event.getClickedBlock().getType(), location, user, C_PRIVATE.guardType, triplet.getSecond(), triplet.getThird());
                break;
            case C_PUBLIC:
                this.manager.createGuard(event.getClickedBlock().getType(), location, user, C_PUBLIC.guardType, triplet.getSecond(), false);
                break;
            case C_DONATION:
                this.manager.createGuard(event.getClickedBlock().getType(), location, user, C_DONATION.guardType, triplet.getSecond(), triplet.getThird());
                break;
            case C_FREE:
                this.manager.createGuard(event.getClickedBlock().getType(), location, user, C_FREE.guardType, triplet.getSecond(), triplet.getThird());
                break;
            case C_GUARDED:
                this.manager.createGuard(event.getClickedBlock().getType(), location, user, C_GUARDED.guardType, triplet.getSecond(), triplet.getThird());
                break;
            case INFO:
                guard.showInfo(user);
                break;
            case MODIFY:
                this.manager.modifyGuard(guard, user, triplet.getSecond(), triplet.getThird());
                break;
            case REMOVE:
                this.manager.removeGuard(guard, user, false);
                break;
            case UNLOCK:
                guard.unlock(user, location, triplet.getSecond());
                break;
            case INVALIDATE_KEYS:
                if (!guard.isOwner(user))
                {
                    user.sendTranslated("&cThis is not your protection!");
                }
                else if (guard.hasPass())
                {
                    user.sendTranslated("&eYou cannot invalidate BookKeys for password protected chests. &aChange the password to invalidate them!");
                }
                else
                {
                    this.manager.invalidateKeyBooks(guard);
                    if (event.getClickedBlock().getState() instanceof InventoryHolder)
                    {
                        for (HumanEntity viewer : ((InventoryHolder)event.getClickedBlock().getState()).getInventory().getViewers())
                        {
                            viewer.closeInventory();
                        }
                    }
                }
                break;
            case KEYS:
                if (!guard.isOwner(user))
                {
                    user.sendTranslated("&cThis is not your protection!");
                }
                else
                {
                    if (guard.isPublic())
                    {
                        user.sendTranslated("&eThis container is public!");
                    }
                    else
                    {
                        this.manager.attemptCreatingKeyBook(guard, user, triplet.getThird());
                    }
                }
                break;
            }
            this.cmdUsed(user);
            event.setCancelled(true);
        }
    }

    private void cmdUsed(User user)
    {
        if (!this.persist.contains(user.getName()))
        {
            this.map.remove(user.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClickEntity(PlayerInteractEntityEvent event)
    {
        if (!map.keySet().contains(event.getPlayer().getName())) return;
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        Location location = event.getRightClicked().getLocation();
        Triplet<CommandType, String, Boolean> triplet = map.get(user.getName());
        Guard guard = this.manager.getGuardForEntityUID(event.getRightClicked().getUniqueId(), triplet.getFirst() != INFO);
        if (triplet.getFirst().isCreator())
        {
            if (guard != null)
            {
                user.sendTranslated("&eThis entity is already protected!");
                this.cmdUsed(user);
                event.setCancelled(true);
                return;
            }
            else
            {
                if (!(event.getRightClicked() instanceof InventoryHolder))
                {
                    switch (triplet.getFirst())
                    {
                    case C_DONATION:
                    case C_FREE:
                    case C_GUARDED:
                        user.sendTranslated("&eYou can only apply guarded, donation and free protections to inventory-holders!");
                        event.setCancelled(true);
                        this.cmdUsed(user);
                        return;
                    }
                }
                if (!this.manager.canProtect(event.getRightClicked().getType()))
                {
                    this.cmdUsed(user);
                    user.sendTranslated("&cYou cannot protect this!");
                    return; // do nothing entity is not protectable
                }
            }
        }
        else if (guard == null)
        {
            user.sendTranslated("&6No protection detected here!");
            event.setCancelled(true);
            this.cmdUsed(user);
            return;
        }
        switch (triplet.getFirst())
        {
        case C_PRIVATE:
            this.manager.createGuard(event.getRightClicked(), user, C_PRIVATE.guardType, triplet.getSecond(), triplet.getThird());
            break;
        case C_PUBLIC:
            this.manager.createGuard(event.getRightClicked(), user, C_PUBLIC.guardType, triplet.getSecond(), triplet.getThird());
            break;
        case C_DONATION:
            this.manager.createGuard(event.getRightClicked(), user, C_DONATION.guardType, triplet.getSecond(), triplet.getThird());
            break;
        case C_FREE:
            this.manager.createGuard(event.getRightClicked(), user, C_FREE.guardType, triplet.getSecond(), triplet.getThird());
            break;
        case C_GUARDED:
            this.manager.createGuard(event.getRightClicked(), user, C_GUARDED.guardType, triplet.getSecond(), triplet.getThird());
            break;
        case INFO:
            guard.showInfo(user);
            break;
        case MODIFY:
            this.manager.modifyGuard(guard, user, triplet.getSecond(), triplet.getThird());
            break;
        case REMOVE:
            this.manager.removeGuard(guard, user, false);
            break;
        case UNLOCK:
            guard.unlock(user, location, triplet.getSecond());
            break;
        case INVALIDATE_KEYS:

            if (!guard.isOwner(user))
            {
                user.sendTranslated("&cThis is not your protection!");
            }
            else if (guard.hasPass())
            {
                user.sendTranslated("&eYou cannot invalidate BookKeys for password protected chests. &aChange the password to invalidate them!");
            }
            else
            {
                this.manager.invalidateKeyBooks(guard);
                if (event.getRightClicked() instanceof InventoryHolder)
                {
                    for (HumanEntity viewer : ((InventoryHolder)event.getRightClicked()).getInventory().getViewers())
                    {
                        viewer.closeInventory();
                    }
                }
            }
            break;
        case KEYS:
            if (!guard.isOwner(user))
            {
                user.sendTranslated("&cThis is not your protection!");
            }
            else
            {
                if (guard.isPublic())
                {
                    user.sendTranslated("&eThis container is public!");
                }
                else
                {
                    this.manager.attemptCreatingKeyBook(guard, user, triplet.getThird());
                }
            }
            break;
        }
        this.cmdUsed(user);
        event.setCancelled(true);
    }

    public enum CommandType
    {
        C_PRIVATE(PRIVATE),
        C_PUBLIC(PUBLIC),
        C_DONATION(DONATION),
        C_FREE(FREE),
        C_GUARDED(GUARDED),
        // TODO
        INFO,
        MODIFY,
        REMOVE,
        UNLOCK,
        INVALIDATE_KEYS,
        KEYS
        ;

        private CommandType(GuardType guardType)
        {
            this.guardType = guardType;
        }

        private CommandType()
        {
            guardType = null;
        }

        public final GuardType guardType;

        public boolean isCreator()
        {
            return guardType != null;
        }
    }
}
