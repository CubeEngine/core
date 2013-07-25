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
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;

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
        THashMap<User,String> userStrings = new THashMap<User, String>();
        ArrayList<User> onlineList = new ArrayList<User>();
        ArrayList<User> afkList = new ArrayList<User>();
        for (User user : users)
        {
            if ((sender instanceof User) && !((User)sender).canSee(user)) // TODO add a permission and a marker for hiddens
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
                onlineList.add(user);
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
        Collections.sort(onlineList,comparator);
        Collections.sort(afkList,comparator);
        onlineList.addAll(afkList);

        DisplayOnlinePlayerListEvent event = new DisplayOnlinePlayerListEvent(sender, userStrings, onlineList);
        if (!(this.module.getCore().getEventManager().fireEvent(event)).isCancelled()) // catch this event to change / show list with extra data
        {
            sender.sendTranslated("&9Players online: &a%d&f/&e%d", event.getUserStrings().size(), Bukkit.getMaxPlayers());
            for (Entry<String,List<User>> entry : event.getGrouped().entrySet())
            {
                String group = entry.getKey()+ChatFormat.parseFormats("&f: ");
                List<String> displayNames = new ArrayList<String>();
                for (User user : entry.getValue())
                {
                    displayNames.add(event.getUserStrings().get(user));
                }
                group += StringUtils.implode("&f, &2",displayNames);
                sender.sendMessage(ChatFormat.parseFormats(group));
            }
        }
    }
}
