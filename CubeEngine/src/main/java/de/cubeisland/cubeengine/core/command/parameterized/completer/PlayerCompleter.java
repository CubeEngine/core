package de.cubeisland.cubeengine.core.command.parameterized.completer;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.parameterized.Completer;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.user.User;

import java.util.ArrayList;
import java.util.List;

import static de.cubeisland.cubeengine.core.util.StringUtils.startsWithIgnoreCase;

public class PlayerCompleter implements Completer
{
    private static boolean canSee(CommandSender sender, User user)
    {
        if (sender instanceof User)
        {
            return ((User)sender).canSee(user);
        }
        return true;
    }

    @Override
    public List<String> complete(CommandSender sender, String token)
    {
        List<String> playerNames = new ArrayList<String>();
        for (User player : CubeEngine.getUserManager().getOnlineUsers())
        {
            String name = player.getName();
            if (canSee(sender,  player) && startsWithIgnoreCase(name, token))
            {
                playerNames.add(name);
            }
        }
        playerNames.remove(sender.getName());

        return playerNames;
    }
}
