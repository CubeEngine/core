package de.cubeisland.cubeengine.basics.command.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsAttachment;

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
        // TODO CE-94 list sorting by roles
        Set<User> users = context.getCore().getUserManager().getOnlineUsers();
        if (users.isEmpty())
        {
            context.sendTranslated("&cThere are no players online now!");
            return;
        }
        THashMap<User,String> userStrings = new THashMap<User, String>();
        ArrayList<User> onlineList = new ArrayList<User>();
        ArrayList<User> afkList = new ArrayList<User>();
        for (User user : users)
        {
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
            userStrings.put(user,s);
        }
        Comparator<User> comparator = new Comparator<User>()
        {
            @Override
            public int compare(User o1, User o2)
            {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getDisplayName(),o2.getDisplayName());
            }
        };
        Collections.sort(onlineList,comparator);
        Collections.sort(afkList,comparator);
        onlineList.addAll(afkList);

        DisplayOnlinePlayerListEvent event = new DisplayOnlinePlayerListEvent(this.module, context.getSender(), userStrings, onlineList);
        if (event.isCancelled())
        {
            return;
        }
        if (!(this.module.getCore().getEventManager().fireEvent(event)).isCancelled()) // catch this event to change / show list with extra data
        {
            context.sendTranslated("&9Players online: &a%d&f/&e%d", event.getUserStrings().size(), Bukkit.getMaxPlayers());
            for (Entry<String,List<User>> entry : event.getGrouped().entrySet())
            {
                String group = entry.getKey();
                List<String> displayNames = new ArrayList<String>();
                for (User user : entry.getValue())
                {
                    displayNames.add(event.getUserStrings().get(user));
                }
                group += StringUtils.implode("&f, &2",displayNames);
                context.getSender().sendMessage(ChatFormat.parseFormats(group));
            }
        }
    }
}
