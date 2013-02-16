package de.cubeisland.cubeengine.core.command.parameterized.completer;

import de.cubeisland.cubeengine.core.command.parameterized.ParamCompleter;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static de.cubeisland.cubeengine.core.util.StringUtils.startsWithIgnoreCase;

public class PlayerCompleter extends ParamCompleter
{
    private final UserManager um;

    public PlayerCompleter(UserManager um)
    {
        super(Player.class, User.class);
        this.um = um;
    }

    private boolean canSee(CommandSender sender, User user)
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
        for (User player : this.um.getOnlineUsers())
        {
            String name = player.getName();
            if (this.canSee(sender,  player) && startsWithIgnoreCase(name, token))
            {
                playerNames.add(name);
            }
        }
        playerNames.remove(sender.getName());

        return playerNames;
    }
}
