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
package org.cubeengine.libcube.service.command;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.CommandDescriptor;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.Dispatcher;
import org.cubeengine.butler.DispatcherCommand;
import org.cubeengine.butler.SimpleCommandDescriptor;
import org.cubeengine.butler.alias.AliasCommand;
import org.cubeengine.butler.parametric.ParametricContainerCommand;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.i18n.formatter.MessageType;
import org.cubeengine.libcube.service.i18n.formatter.component.HoverComponent;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextFormat;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEUTRAL;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NONE;
import static org.spongepowered.api.text.format.TextColors.*;

public class HelpCommand implements CommandBase
{
    private static final SimpleCommandDescriptor helpDescriptor = new SimpleCommandDescriptor();
    static
    {
        helpDescriptor.setName("?");
        helpDescriptor.setDescription("Displays Help");
    }
    private Dispatcher helpTarget;
    private I18n i18n;

    public HelpCommand(Dispatcher target, I18n i18n)
    {
        this.helpTarget = target;
        this.i18n = i18n;
    }

    @Override
    public boolean execute(CommandInvocation invocation)
    {
        if (!(invocation.getCommandSource() instanceof CommandSource))
        {
            return false;
        }
        CommandDescriptor descriptor = helpTarget.getDescriptor();
        CommandSource sender = (CommandSource)invocation.getCommandSource();

        TextFormat formatGray = NONE.color(GRAY);
        i18n.sendTranslated(sender, formatGray, "Description: {input}", i18n.getTranslation(sender, MessageType.NONE, descriptor.getDescription()).toPlain());

        List<String> labels = new ArrayList<>(invocation.getLabels());
        if (labels.isEmpty())
        {
            labels.add("");
        }
        if ("?".equals(labels.get(labels.size() - 1)))
        {
            labels.remove(labels.size() - 1);
        }

        i18n.sendTranslated(sender, formatGray, "Usage: {input}", descriptor.getUsage(invocation, labels.toArray(new String[labels.size()])));
        sender.sendMessage(Text.of());

        if (helpTarget instanceof DispatcherCommand)
        {
            Set<CommandBase> commands = helpTarget.getCommands();
            if (!commands.isEmpty() && (commands.size() != 1
                || !(commands.iterator().next() instanceof HelpCommand))) // is Empty ignoring HelpCommand
            {
                i18n.sendTranslated(sender, NEUTRAL, "The following sub-commands are available:");
                sender.sendMessage(Text.of());
                commands.stream()
                        .filter(command -> !(command instanceof HelpCommand || command instanceof AliasCommand
                                            && commands.contains(((AliasCommand)command).getTarget())))
                        .filter(command -> !(command.getDescriptor() instanceof CubeCommandDescriptor
                                          && ((CubeCommandDescriptor)command.getDescriptor()).isCheckPerm()
                                          && !sender.hasPermission(((CubeCommandDescriptor)command.getDescriptor()).getPermission().getName())))
                    .forEach(command -> sender.sendMessage(Text.of(YELLOW, command.getDescriptor().getName()).toBuilder().onClick(
                        TextActions.runCommand("/" + (String.join(" ", labels) + " " + command.getDescriptor().getName()).trim() + " ?")).append(Text.of(WHITE, ": ", GRAY,
                                                               i18n.getTranslation(sender, TextFormat.NONE, command.getDescriptor().getDescription()))).build()));
                sender.sendMessage(Text.of());
            }
            else if (helpTarget instanceof ParametricContainerCommand)
            {
                i18n.sendTranslated(sender, MessageType.NEGATIVE, "No actions are available");
                sender.sendMessage(Text.of());
            }
        }

        // TODO currently we have nothing here:
        /*
        if (descriptor instanceof CubeDescriptor)
        {
            sender.sendTranslated(GRAY, "Detailed help: {input#link:color=INDIGO}", "http://engine.cubeisland.de/c/" + ((CubeDescriptor)descriptor).getModule().getInformation().getName().toLowerCase() + "/" + StringUtils.implode("/", labels));
        }
        */
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
    TODO ConversationHelp - Click help has to be without slash
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
