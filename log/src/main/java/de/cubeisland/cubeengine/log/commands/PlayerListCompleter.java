package de.cubeisland.cubeengine.log.commands;

import java.util.ArrayList;
import java.util.List;

import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.parameterized.Completer;
import de.cubeisland.cubeengine.core.user.User;

public class PlayerListCompleter implements Completer
{
    @Override
    public List<String> complete(CommandSender sender, String token)
    {
        List<String> result = new ArrayList<String>();
        String lastToken = token;
        String firstTokens = "";
        if (lastToken.contains(","))
        {
            firstTokens = lastToken.substring(0, lastToken.lastIndexOf(",")+1);
            lastToken = lastToken.substring(lastToken.lastIndexOf(",")+1,lastToken.length());
        }
        if (lastToken.startsWith("!"))
        {
            lastToken = lastToken.substring(1, lastToken.length());
            firstTokens += "!";
        }

        for (User user : sender.getCore().getUserManager().getLoadedUsers())
        {
            if (user.getName().startsWith(lastToken))
            {
                if (!token.contains(user.getName()+","))
                {
                    result.add(firstTokens + user.getName());
                }
            }
        }
        return result;
    }
}
