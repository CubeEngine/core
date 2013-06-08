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
package de.cubeisland.cubeengine.roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.basics.command.general.DisplayOnlinePlayerListEvent;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RolesAttachment;
import de.cubeisland.cubeengine.roles.role.RolesManager;

public class BasicsOnlinePlayerList implements Listener
{
    private final RolesManager manager;
    private final Roles module;

    public BasicsOnlinePlayerList(Roles module)
    {
        this.module = module;
        this.manager = this.module.getRolesManager();
    }

    @EventHandler
    public void onOnlinePlayerList(DisplayOnlinePlayerListEvent event)
    {
        String noRole = ChatFormat.parseFormats("&7No Role");
        Map<String,List<User>> grouped = event.getGrouped();
        grouped.clear();
        for (User user : event.getDefaultList())
        {
            RolesAttachment attachment = user.get(RolesAttachment.class);
            if (attachment == null)
            {
                List<User> users = grouped.get(noRole);
                if (users == null)
                {
                    users = new ArrayList<User>();
                    grouped.put(noRole,users);
                }
                users.add(user);
            }
            else
            {
                Role role = attachment.getDominantRole();
                String display;
                if (role.getRawMetadata().get("prefix") == null)
                {
                    display = "&7"+role.getName();
                }
                else
                {
                    display = role.getRawMetadata().get("prefix");
                }
                display = ChatFormat.parseFormats(display);
                List<User> users = grouped.get(display);
                if (users == null)
                {
                    users = new ArrayList<User>();
                    grouped.put(display,users);
                }
                users.add(user);
            }
        }
    }
}
