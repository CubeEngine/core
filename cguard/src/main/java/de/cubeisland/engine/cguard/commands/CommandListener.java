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
package de.cubeisland.engine.cguard.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.cubeisland.engine.cguard.Cguard;
import de.cubeisland.engine.cguard.storage.Guard;
import de.cubeisland.engine.cguard.storage.GuardManager;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.cguard.commands.CommandListener.CommandType.C_PRIVATE;
import static de.cubeisland.engine.cguard.storage.TableGuards.GUARDTYPE_PRIVATE;
import static de.cubeisland.engine.cguard.storage.TableGuards.GUARDTYPE_PUBLIC;

public class CommandListener implements Listener
{
    private Map<String, CommandType> map = new HashMap<>();
    private Set<String> persist = new HashSet<>();

    private Cguard module;
    private GuardManager manager;

    public CommandListener(Cguard module, GuardManager manager)
    {
        this.module = module;
        this.manager = manager;
    }

    public void setCommandType(User user, CommandType guardtype)
    {
        map.put(user.getName(), guardtype);
        if (persist.contains(user.getName()))
        {
            user.sendTranslated("&ePersist mode is active. Your command will be repeated until reusing &6/cpersist");
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
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getClickedBlock() != null)
        {
            // TODO check if block is allowed for protections
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            Guard guard;
            switch (map.get(user.getName()))
            {
            case C_PRIVATE:
                guard = this.manager.createGuard(event.getClickedBlock().getType(), event.getClickedBlock().getLocation(), user, C_PRIVATE.guardType);
                user.sendTranslated("&cPrivate Protection Created!");
                // TODO print short info
                break;
            case C_PUBLIC:
                break;
            case INFO:
                guard = this.manager.getGuardAtLocation(event.getClickedBlock().getLocation());
                if (guard == null)
                {
                    user.sendTranslated("&6No protection detected here!");
                }
                else
                {
                    user.sendTranslated("PROTECTION FOUND: TODO INFO"); // TODO
                }
                break;
            }
            if (!this.persist.contains(user.getName()))
            {
                this.map.remove(user.getName());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRightClickEntity(PlayerInteractEntityEvent event)
    {
        if (!map.keySet().contains(event.getPlayer().getName())) return;

        // TODO check if entity is allowed
    }

    public enum CommandType
    {
        C_PRIVATE(GUARDTYPE_PRIVATE),
        C_PUBLIC(GUARDTYPE_PUBLIC),
        // TODO
        INFO;

        private CommandType(byte guardType)
        {
            this.guardType = guardType;
        }

        private CommandType()
        {
            guardType = null;
        }

        public final Byte guardType;

    }

}
