package de.cubeisland.cubeengine.core.command.parameterized;

import de.cubeisland.cubeengine.core.command.sender.CommandSender;

import java.util.List;

public interface Completer
{
    List<String> complete(CommandSender sender, String token);
}
