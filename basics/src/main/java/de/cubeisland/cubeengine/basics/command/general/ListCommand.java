package de.cubeisland.cubeengine.basics.command.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.basics.Basics;

public class ListCommand
{
    private final Basics module;

    public ListCommand(Basics module) {
        this.module = module;
    }

    @Command(desc = "Displays all the online players.")
    public void list(CommandContext context)
    {
        // TODO CE-90 beautify list
        // TODO CE-64 list sorting by roles
        List<User> users = context.getCore().getUserManager().getOnlineUsers();
        if (users.isEmpty())
        {
            context.sendTranslated("&cThere are no players online now!");
            return;
        }
        List<String> userNames = new ArrayList<String>();
        for (User user : users)
        {
            userNames.add(user.getName());
        }
        DisplayOnlinePlayerListEvent event = new DisplayOnlinePlayerListEvent(this.module, context.getSender(), users, userNames);
        if (event.isCancelled())
        {
            return;
        }
        if (!(this.module.getCore().getEventManager().fireEvent(event)).isCancelled()) // catch this event to change / show list with extra data
        {
            context.sendTranslated("&9Players online: &a%d&f/&e%d", event.getUsers().size(), Bukkit.getMaxPlayers());
            context.sendTranslated("&ePlayers:\n&2%s", this.displayPlayerList(event));
        }
    }

    public String displayPlayerList(DisplayOnlinePlayerListEvent event)
    {
        String delim = ChatFormat.parseFormats("&f, &2");
        return StringUtils.implode(delim,event.getUserNames());
    }
}
