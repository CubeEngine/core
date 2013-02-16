package de.cubeisland.cubeengine.core.command.parameterized.completer;

import de.cubeisland.cubeengine.core.command.parameterized.Completer;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.util.matcher.Match;

import java.util.Arrays;
import java.util.List;

public class ItemCompleter implements Completer
{
    @Override
    public List<String> complete(CommandSender sender, String token)
    {
        return Arrays.asList(String.valueOf(Match.material().material(token).getId()));
    }
}
