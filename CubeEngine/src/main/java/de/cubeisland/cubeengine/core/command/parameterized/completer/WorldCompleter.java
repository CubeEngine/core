package de.cubeisland.cubeengine.core.command.parameterized.completer;

import de.cubeisland.cubeengine.core.command.parameterized.Completer;
import de.cubeisland.cubeengine.core.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

import static de.cubeisland.cubeengine.core.util.StringUtils.startsWithIgnoreCase;

public class WorldCompleter implements Completer
{
    private final Server server = Bukkit.getServer();

    @Override
    public List<String> complete(CommandSender sender, String token)
    {
        List<String> offers = new ArrayList<String>();
        for (World world : this.server.getWorlds())
        {
            final String name = world.getName();
            if (startsWithIgnoreCase(name, token))
            {
                offers.add(name);
            }
        }
        return offers;
    }
}
