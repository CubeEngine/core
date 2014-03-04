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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Bukkit;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.core.command.BasicContextFactory;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;

public class ListCommand extends CubeCommand
{
    protected static final Comparator<User> USER_COMPARATOR = new UserComparator();
    private final Basics basics;

    public ListCommand(Basics basics)
    {
        super(basics, "list", "Displays all the online players.", new BasicContextFactory());
        this.basics = basics;
    }

    protected SortedMap<String, Set<User>> groupUsers(Set<User> users)
    {
        SortedMap<String, Set<User>> grouped = new TreeMap<>();
        grouped.put("&6Players", users);

        return grouped;
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        final CommandSender sender = context.getSender();
        final SortedSet<User> users = new TreeSet<>(USER_COMPARATOR);

        for (User user : context.getCore().getUserManager().getOnlineUsers())
        {
            if (sender instanceof User && !((User)sender).canSee(user))
            {
                continue;
            }
            users.add(user);
        }

        if (users.isEmpty())
        {
            sender.sendTranslated("&cThere are no players online at the moment!");
            return null;
        }

        SortedMap<String, Set<User>> grouped = this.groupUsers(users);
        sender.sendTranslated("&9Players online: &a%d&f/&e%d", users.size(), Bukkit.getMaxPlayers());

        for (Entry<String, Set<User>> entry : grouped.entrySet())
        {
            Iterator<User> it = entry.getValue().iterator();
            if (!it.hasNext())
            {
                continue;
            }
            StringBuilder group = new StringBuilder(entry.getKey()).append("&f: ");
            group.append(this.formatUser(it.next()));

            while (it.hasNext())
            {
                group.append("&f, ").append(this.formatUser(it.next()));
            }
            sender.sendMessage(ChatFormat.parseFormats(group.toString()));
        }

        return null;
    }

    private String formatUser(User user)
    {
        String entry = "&2" + user.getDisplayName();
        if (user.attachOrGet(BasicsAttachment.class, basics).isAfk())
        {
            entry += "&f(&7afk&f)";
        }
        return entry;
    }

    private static final class UserComparator implements Comparator<User>
    {
        @Override
        public int compare(User user1, User user2)
        {
            return String.CASE_INSENSITIVE_ORDER.compare(user1.getDisplayName(), user2.getDisplayName());
        }
    }
}
