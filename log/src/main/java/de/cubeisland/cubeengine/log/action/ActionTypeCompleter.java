package de.cubeisland.cubeengine.log.action;

import java.util.ArrayList;
import java.util.List;

import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.parameterized.Completer;

public class ActionTypeCompleter implements Completer
{
    static ActionTypeManager manager;

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
        for (String s : manager.getAllActionAndCategoryStrings())
        {
            if (s.startsWith(lastToken))
            {
                if (!token.contains(s+","))
                {
                    result.add(firstTokens + s);
                }
            }
        }
        return result;
    }
}
