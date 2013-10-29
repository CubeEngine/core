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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.roles.role.Role;
import de.cubeisland.engine.roles.role.RolesAttachment;
import gnu.trove.map.hash.THashMap;

public class ListCommand
{
    private final Basics module;

    public ListCommand(Basics module) {
        this.module = module;
    }

    @Command(desc = "Displays all the online players.")
    public void list(CommandContext context)
    {
        CommandSender sender = context.getSender();
        Set<User> users = context.getCore().getUserManager().getOnlineUsers();
        if (users.isEmpty())
        {
            sender.sendTranslated("&cThere are no players online now!");
            return;
        }
        THashMap<User,String> userStrings = new THashMap<>();
        ArrayList<User> defaultList = new ArrayList<>();
        ArrayList<User> afkList = new ArrayList<>();
        for (User user : users)
        {
            if ((sender instanceof User) && !((User)sender).canSee(user))
            {
                continue;
            }
            String s = "&2"+user.getDisplayName();
            if (user.attachOrGet(BasicsAttachment.class,this.module).isAfk())
            {
                s += "&f(&7afk&f)";
                afkList.add(user);
            }
            else
            {
                defaultList.add(user);
            }
            userStrings.put(user, s);
        }
        Comparator<User> comparator = new Comparator<User>()
        {
            @Override
            public int compare(User user1, User user2)
            {
                return String.CASE_INSENSITIVE_ORDER.compare(user1.getDisplayName(), user2.getDisplayName());
            }
        };
        Collections.sort(defaultList,comparator);
        Collections.sort(afkList,comparator);
        defaultList.addAll(afkList);
        Map<String, List<User>> grouped;
        sender.sendTranslated("&9Players online: &a%d&f/&e%d", userStrings.size(), Bukkit.getMaxPlayers());
        if (this.module.getRolesModule() != null)
        {
            List<User> noRoleList = new ArrayList<>();
            grouped = new LinkedHashMap<>();
            TreeMap<Role, List<User>> groupedRoles = new TreeMap<>(new Comparator<Role>()
            {
                @Override
                public int compare(Role o1, Role o2)
                {
                    return o1.getPriorityValue() - o2.getPriorityValue();
                }
            });
            for (User user : defaultList)
            {
                RolesAttachment attachment = user.get(RolesAttachment.class);
                if (attachment == null)
                {
                    noRoleList.add(user);
                }
                else
                {
                    Role role = attachment.getDominantRole();
                    List<User> list = groupedRoles.get(role);
                    if (list == null)
                    {
                        list = new ArrayList<>();
                        groupedRoles.put(role,list);
                    }
                    list.add(user);
                }
            }
            for (Entry<Role, List<User>> entry : groupedRoles.entrySet())
            {
                String display;
                if (entry.getKey().getRawMetadata().get("prefix") == null)
                {
                    display = "&7"+entry.getKey().getName();
                }
                else
                {
                    display = entry.getKey().getRawMetadata().get("prefix");
                }
                display = ChatFormat.parseFormats(display);
                grouped.put(display, entry.getValue());
            }
            grouped.put(ChatFormat.parseFormats("&7No Role"), noRoleList);
        }
        else
        {
            grouped = new HashMap<>();
            grouped.put("&6Players", defaultList);
        }
        for (Entry<String,List<User>> entry : grouped.entrySet())
        {
            if (entry.getValue().isEmpty()) continue;
            String group = entry.getKey()+ChatFormat.parseFormats("&f: ");
            List<String> displayNames = new ArrayList<>();
            for (User user : entry.getValue())
            {
                displayNames.add(userStrings.get(user));
            }
            group += StringUtils.implode("&f, &2",displayNames);
            sender.sendMessage(ChatFormat.parseFormats(group));
        }

    }
}
