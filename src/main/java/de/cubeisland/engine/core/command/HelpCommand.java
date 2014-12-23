/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.command;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.cubeisland.engine.command.CommandBase;
import de.cubeisland.engine.command.CommandDescriptor;
import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.Dispatcher;
import de.cubeisland.engine.command.DispatcherCommand;
import de.cubeisland.engine.command.ImmutableCommandDescriptor;
import de.cubeisland.engine.command.Name;
import de.cubeisland.engine.command.alias.AliasCommand;
import de.cubeisland.engine.command.alias.AliasConfiguration;
import de.cubeisland.engine.command.alias.Aliases;
import de.cubeisland.engine.command.methodic.MethodicCommandContainer;
import de.cubeisland.engine.command.parameter.property.Description;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.formatter.MessageType;

import static de.cubeisland.engine.core.util.ChatFormat.*;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public class HelpCommand implements CommandBase
{
    private Dispatcher helpTarget;

    private static final ImmutableCommandDescriptor helpDescriptor = new ImmutableCommandDescriptor();

    static
    {
        helpDescriptor.setProperty(new Name("?"));
        helpDescriptor.setProperty(new Aliases(Collections.<AliasConfiguration>emptyList()));
        helpDescriptor.setProperty(new Description("Displays Help"));
    }

    public HelpCommand(Dispatcher target)
    {
        this.helpTarget = target;
    }

    @Override
    public boolean execute(CommandInvocation invocation)
    {
        if (!(invocation.getCommandSource() instanceof CommandSender))
        {
            return false;
        }
        CommandSender sender = (CommandSender)invocation.getCommandSource();
        MessageType grey = MessageType.of(GREY);
        sender.sendTranslated(grey, "Description: {input}", sender.getTranslation(NONE, helpTarget.getDescriptor().getDescription()));
        ArrayList<String> labels = new ArrayList<>(invocation.getLabels());
        if ("?".equals(labels.get(labels.size() - 1)))
        {
            labels.remove(labels.size() - 1);
        }
        sender.sendTranslated(grey, "Usage: {input}", helpTarget.getDescriptor().getUsage(invocation));
        sender.sendMessage(" ");

        if (helpTarget instanceof DispatcherCommand)
        {
            Set<CommandBase> commands = helpTarget.getCommands();
            if (!commands.isEmpty() && (commands.size() != 1
                || !(commands.iterator().next() instanceof HelpCommand))) // is Empty ignoring HelpCommand
            {
                sender.sendTranslated(NEUTRAL, "The following sub-commands are available:");
                sender.sendMessage(" ");
                for (CommandBase command : commands)
                {
                    if (command instanceof HelpCommand
                     || command instanceof AliasCommand && commands.contains(((AliasCommand)command).getTarget()))
                    {
                        continue;
                    }
                    sender.sendMessage(YELLOW + command.getDescriptor().getName() + WHITE + ": " +
                                         GREY + sender.getTranslation(NONE, command.getDescriptor().getDescription()));
            }
                sender.sendMessage(" ");
            }
            else if (helpTarget instanceof MethodicCommandContainer)
            {
                sender.sendTranslated(NEGATIVE, "No actions are available");
                sender.sendMessage(" ");
            }
        }

        sender.sendTranslated(grey, "Detailed help: {input#link:color=INDIGO}",
                              "http://engine.cubeisland.de/c/" + helpTarget.getDescriptor().valueFor(
                                  ModuleProvider.class).getId() + "/" + StringUtils.implode("/", labels));
        return true;
    }

    @Override
    public CommandDescriptor getDescriptor()
    {
        return helpDescriptor;
    }

    @Override
    public List<String> getSuggestions(CommandInvocation call)
    {
        return null; // No Suggestions
    }

    /*
    TODO ConversationHelp
    context.sendTranslated(NEUTRAL, "Flags:");
            Set<String> flags = new HashSet<>();
            for (FlagParameter flag : helpTarget.getContextFactory().descriptor().getFlags())
            {
                flags.add(flag.getLongName().toLowerCase());
            }
            context.sendMessage("    " + StringUtils.implode(ChatFormat.GREY + ", " + ChatFormat.WHITE, flags));
            context.sendTranslated(NEUTRAL, "Parameters:");
            Set<String> params  = new HashSet<>();
            for (NamedParameter param : helpTarget.getContextFactory().descriptor().getNamedGroups().listAll())
            {
                params.add(param.getName().toLowerCase());
            }
            context.sendMessage("    " + StringUtils.implode(ChatFormat.GREY + ", " + ChatFormat.WHITE, params));
            return null;
     */
}
