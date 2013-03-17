package de.cubeisland.cubeengine.core.command.chatcommand;

import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.CommandSender;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class ChatCommandContext extends ParameterizedContext
{
    public ChatCommandContext(CubeCommand command, CommandSender sender, Stack<String> labels, Set<String> flags, Map<String, Object> params) {
        super(command, sender, labels, new LinkedList<String>(), flags, params);
    }
}
