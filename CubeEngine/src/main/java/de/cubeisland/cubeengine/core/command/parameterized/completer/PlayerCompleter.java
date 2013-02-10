package de.cubeisland.cubeengine.core.command.parameterized.completer;

import de.cubeisland.cubeengine.core.command.parameterized.ParamCompleter;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PlayerCompleter extends ParamCompleter
{
    private final UserManager um;

    public PlayerCompleter(UserManager um)
    {
        super(Player.class, User.class);
        this.um = um;
    }

    @Override
    public List<String> complete(User sender, String token)
    {
        List<String> playerNames = new ArrayList<String>();
        for (User player : this.um.getOnlineUsers())
        {
            String name = player.getName();
            if (sender.canSee(player) && StringUtil.startsWithIgnoreCase(name, token))
            {
                playerNames.add(name);
            }
        }
        playerNames.remove(sender.getName());
        Collections.sort(playerNames, String.CASE_INSENSITIVE_ORDER);

        return playerNames;
    }
}
