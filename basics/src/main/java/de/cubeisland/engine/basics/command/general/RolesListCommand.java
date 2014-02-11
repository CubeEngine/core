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
package de.cubeisland.engine.basics.command.general;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.roles.role.Role;
import de.cubeisland.engine.roles.role.RolesAttachment;
import gnu.trove.set.hash.THashSet;

public class RolesListCommand extends ListCommand
{
    private static final Comparator<Role> ROLE_COMPARATOR = new RoleComparator();

    public RolesListCommand(Basics basics)
    {
        super(basics);
        setDescription("Displays all the online players ordered by role.");
    }

    @Override
    protected SortedMap<String, Set<User>> groupUsers(Set<User> users)
    {
        Set<User> noRoleSet = new THashSet<>();
        TreeMap<Role, Set<User>> groupedRoles = new TreeMap<>(ROLE_COMPARATOR);
        for (User user : users)
        {
            RolesAttachment attachment = user.get(RolesAttachment.class);
            if (attachment == null)
            {
                noRoleSet.add(user);
            }
            else
            {
                Role role = attachment.getDominantRole();
                if (role == null)
                {
                    noRoleSet.add(user);
                }
                else
                {
                    Set<User> list = groupedRoles.get(role);
                    if (list == null)
                    {
                        groupedRoles.put(role, list = new THashSet<>());
                    }
                    list.add(user);
                }
            }
        }

        SortedMap<String, Set<User>> grouped = new TreeMap<>();
        Role role;
        for (Entry<Role, Set<User>> entry : groupedRoles.entrySet())
        {
            role = entry.getKey();
            String display;
            if (role.getRawMetadata().get("prefix") != null)
            {
                display = role.getRawMetadata().get("prefix");
            }
            else
            {
                display = "&7" + role.getName();
            }
            display = ChatFormat.parseFormats(display);
            grouped.put(display, entry.getValue());
        }
        grouped.put(ChatFormat.parseFormats("&7No Role"), noRoleSet);

        return grouped;
    }

    private static final class RoleComparator implements Comparator<Role>
    {
        @Override
        public int compare(Role o1, Role o2)
        {
            return o2.getPriorityValue() - o1.getPriorityValue();
        }
    }
}
