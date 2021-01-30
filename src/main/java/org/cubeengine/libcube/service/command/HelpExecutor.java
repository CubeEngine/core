/*
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.apache.logging.log4j.util.Strings;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.i18n.formatter.MessageType;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.Parameter.Subcommand;
import org.spongepowered.api.event.EventContextKeys;

public class HelpExecutor implements CommandExecutor
{

    private I18n i18n;
    private Command.Parameterized target;
    private CubeEngineCommand executor;
    private String perm;

    public HelpExecutor(I18n i18n)
    {
        this.i18n = i18n;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Optional<String> rawCommand = context.getCause().getContext().get(EventContextKeys.COMMAND);
        final Audience audience = context.getCause().getAudience();
        final Style grayStyle = Style.style(NamedTextColor.GRAY);
        Component descLabel = i18n.translate(audience, grayStyle, "Description:");
        final Component permText = i18n.translate(audience, grayStyle, "Permission: (click to copy) {input}", perm).append(Component.text(".use").color(NamedTextColor.WHITE));
        descLabel = descLabel.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, permText)).clickEvent(ClickEvent.copyToClipboard(perm + ".use"));
        final Component descValue = target.getShortDescription(context.getCause()).get().color(NamedTextColor.GOLD);
        context.sendMessage(Identity.nil(), Component.empty().append(descLabel).append(Component.space()).append(descValue));

        List<String> usages = new ArrayList<>();
        for (Parameter param : target.parameters())
        {
            collectUsage(context, usages, param);
        }

        final String actual = rawCommand.map(r -> r.endsWith("?") ? r.substring(0, r.length() - 1) : r).orElse("missing command context").trim();
        final String usage = usages.isEmpty() && executor == null ? "<command>" : Strings.join(usages, ' ');
        final String joinedUsage = actual + " " + usage;

        i18n.send(audience, grayStyle, "Usage: {input}", joinedUsage);

//            context.sendMessage(target.getUsage(context.getCause()).style(grayStyle));
//            context.sendMessage(Component.text(actual.orElse("no cmd?")));

        final List<Parameter.Subcommand> subcommands = target.subcommands().stream().filter(sc -> !sc.getAliases().iterator().next().equals("?")).collect(Collectors.toList());
        if (!subcommands.isEmpty())
        {
            context.sendMessage(Identity.nil(), Component.empty());
            i18n.send(audience, MessageType.NEUTRAL, "The following sub-commands are available:");
            context.sendMessage(Identity.nil(), Component.empty());
            for (Parameter.Subcommand subcommand : subcommands)
            {
                final String firstAlias = subcommand.getAliases().iterator().next();
                final Command.Parameterized subCmd = subcommand.getCommand();
                TextComponent textPart1 = Component.text(firstAlias, NamedTextColor.YELLOW);
                final Component subPermText = i18n.translate(audience, grayStyle, "Permission: (click to copy) {input}", perm + "." + firstAlias).append(
                    Component.text(".use").color(NamedTextColor.WHITE));
                textPart1 = textPart1.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, subPermText)).clickEvent(
                    ClickEvent.copyToClipboard(perm + "." + firstAlias + ".use"));
                final String newHelpCmd = actual + " " + firstAlias + " ?";
                final TextComponent text = Component.empty().append(textPart1).append(Component.text(": ")).append(subCmd.getShortDescription(context.getCause()).get().style(
                    grayStyle).hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("click to show usage"))).clickEvent(
                    ClickEvent.runCommand(newHelpCmd))
                                                                                                                   // TODO missing command context
                                                                                                                  );

                context.sendMessage(Identity.nil(), text);
            }
        }
        else
        {
            if (this.executor == null)
            {
                i18n.send(audience, MessageType.NEGATIVE, "No actions are available");
            }
        }
        context.sendMessage(Identity.nil(), Component.empty());
        return CommandResult.success();
    }

    private void collectUsage(CommandContext context, List<String> usages, Parameter param)
    {
        if (param instanceof Parameter.Value)
        {
            String usage = ((Parameter.Value<?>)param).getUsage(context.getCause());
            if (!param.isOptional())
            {
                usage = "<" + usage + ">";
            }
            usages.add(usage);
        }
        else if (param instanceof Parameter.Multi)
        {
            final List<String> childUsages = new ArrayList<>();
            for (Parameter childParam : ((Parameter.Multi)param).getChildParameters())
            {
                this.collectUsage(context, childUsages, childParam);
            }

            // Fixing multiple named parameters usage:
            // As SubCommands cannot be optional they are nested in multiple FirstOf-SubCommand combos
            // Essentially [p1 <value>] [p2 <value>] is actually registered as
            // [p1 <value> [p2 <value>]] or [p2 <value>]
            // For this we can go through usages in reverse and remove all previous occurences of a named parameter
            final List<String> repeats = new ArrayList<>();
            Collections.reverse(childUsages);
            for (String childUsage : childUsages)
            {
                for (String repeat : repeats)
                {
                    String optRepeat = " [" + repeat + "]";
                    if (childUsage.endsWith(optRepeat))
                    {
                        childUsage = childUsage.substring(0, childUsage.indexOf(optRepeat));
                    }
                }
                repeats.add(childUsage);
            }
            Collections.reverse(repeats);
            childUsages.clear();
            childUsages.addAll(repeats);

            // TODO different usage for sequence/firstof
            if (param.getClass().getName().contains("FirstOf"))
            {
                if (param.isOptional())
                {
                    usages.add("[" + String.join("] [", childUsages) + "]");
                }
                else
                {
                    usages.add("<" + String.join(" | ", childUsages) + ">");
                }
            }
            else
            {
                if (param.isOptional())
                {
                    usages.add("[" + String.join(" ", childUsages) + "]");
                }
                else
                {
                    usages.add("<" + String.join(" ", childUsages) + ">");
                }
            }

        }
        else if (param instanceof Subcommand)
        {
            final Set<String> aliases = ((Subcommand)param).getAliases();
            final ArrayList<String> subUsage = new ArrayList<>();
            for (Parameter subCmdParam : ((Subcommand)param).getCommand().parameters())
            {
                this.collectUsage(context, subUsage, subCmdParam);
            }
            usages.add(String.join("|", aliases) + " " + String.join(" ", subUsage));

        }
        else
        {
            usages.add("param(" + param.getClass().getSimpleName() + ")");
        }
    }

    public void init(Command.Parameterized target, CubeEngineCommand executor, String perm)
    {
        this.target = target;
        this.executor = executor;
        this.perm = perm;
    }
}
