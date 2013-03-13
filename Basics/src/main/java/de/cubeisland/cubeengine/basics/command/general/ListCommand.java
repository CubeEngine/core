package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import org.bukkit.Bukkit;

import java.util.List;

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
            context.sendMessage("basics", "&cThere are no players online now!");
            return;
        }
        DisplayOnlinePlayerListEvent event = new DisplayOnlinePlayerListEvent(module, context.getSender(), users);
        if (event.isCancelled())
        {
            return;
        }
        if (!(module.getEventManager().fireEvent(event)).isCancelled()) // catch this event to change / show list with extra data
        {
            context.sendMessage("basics", "&9Players online: &a%d&f/&e%d", event.getUsers().size(), Bukkit.getMaxPlayers());
            context.sendMessage("basics", "&ePlayers:\n&2%s", this.displayPlayerList(event.getUsers()));
        }
    }

    public String displayPlayerList(List<User> users)
    {
        //TODO test if it looks good for more players
        //1 line ~ 70 characters
        //6 12 18 (+1)
        StringBuilder sb = new StringBuilder();
        StringBuilder partBuilder = new StringBuilder();
        int pos = 0;
        boolean first = true;
        for (User user : users)
        {
            partBuilder.setLength(0);

            String name = user.getName();
            if (name.length() < 6)
            {
                int k = 6 - name.length();
                partBuilder.append(StringUtils.repeat(" ", k / 2));
                k = k - k / 2;
                partBuilder.append(name);
                partBuilder.append(StringUtils.repeat(" ", k));
                pos += 6;
            }
            else
            {
                if (name.length() < 12)
                {
                    int k = 12 - name.length();
                    partBuilder.append(StringUtils.repeat(" ", k / 2));
                    k = k - k / 2;
                    partBuilder.append(name);
                    partBuilder.append(StringUtils.repeat(" ", k));
                    pos += 12;
                }
                else
                {
                    int k = 16 - name.length();
                    partBuilder.append(StringUtils.repeat(" ", k / 2));
                    k = k - k / 2;
                    partBuilder.append(name);
                    partBuilder.append(StringUtils.repeat(" ", k));
                    pos += 16;
                }
            }
            if (pos >= 70)
            {
                pos = partBuilder.toString().length();
                sb.append("\n");
                first = true;
            }
            if (!first)
            {
                sb.append("&f|&2");
                pos++;
            }
            sb.append(partBuilder.toString());
            first = false;
        }
        return sb.toString();
    }
}
