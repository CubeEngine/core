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
package de.cubeisland.engine.customcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Completer;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.parameterized.ParameterizedTabContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.CommandPermission;
import de.cubeisland.engine.core.command.reflected.context.Flag;
import de.cubeisland.engine.core.command.reflected.context.Flags;
import de.cubeisland.engine.core.command.reflected.context.Grouped;
import de.cubeisland.engine.core.command.reflected.context.IParams;
import de.cubeisland.engine.core.command.reflected.context.Indexed;
import de.cubeisland.engine.core.command.result.paginated.PaginatedResult;
import de.cubeisland.engine.core.command.result.paginated.PaginationIterator;

import static de.cubeisland.engine.core.permission.PermDefault.TRUE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static java.util.Locale.ENGLISH;

public class ManagementCommands extends ContainerCommand
{
    private final Customcommands module;
    private final CustomCommandsConfig config;

    public ManagementCommands(Customcommands module)
    {
        super(module, "customcommands", "Commands to modify custom commands.");
        this.module = module;
        this.config = module.getConfig();
    }

    @Command(desc = "Adds a custom chat command.")
    @IParams({@Grouped(@Indexed(label = "name")),
              @Grouped(value = @Indexed(label = "message"), greedy = true)})
    @Flags({@Flag(name = "force", permDefault = TRUE),
            @Flag(name = "global")})
    @CommandPermission(permDefault = TRUE)
    public void add(ParameterizedContext context)
    {
        String name = context.getArg(0);
        String message = context.getStrings(1);

        if (config.commands.containsKey(name))
        {
            if (context.hasFlag("force"))
            {
                config.commands.put(name, message);
                context.sendTranslated(POSITIVE, "Custom command {input} has successfully been replaced.", "!" + name);
            }
            else
            {
                context.sendTranslated(NEGATIVE, "Custom command {input} already exists. Set the flag {text:-force} if you want to replace the message.", "!" + name);
                return;
            }
        }
        else
        {
            config.commands.put(name.toLowerCase(ENGLISH), message);
            context.sendTranslated(POSITIVE, "Custom command {input} has successfully been added.", "!" + name);
        }
        config.save();
    }

    @Command(desc = "Deletes a custom chat command.")
    @IParams(@Grouped(@Indexed(label = "name", completer = CustomCommandCompleter.class)))
    @Flags(@Flag(name = "global"))
    @CommandPermission(permDefault = TRUE)
    public void delete(ParameterizedContext context)
    {
        String name = context.getArg(0);

        if (config.commands.containsKey(name))
        {
            config.commands.remove(name.toLowerCase(ENGLISH));
            config.save();

            context.sendTranslated(POSITIVE, "Custom command {input} has successfully been deleted.", "!" + name);
        }
        else
        {
            context.sendTranslated(NEGATIVE, "Custom command {input} has not been found.", "!" + name);
        }
    }

    @Command(desc = "Prints out all the custom chat commands.")
    @CommandPermission(permDefault = TRUE)
    public CommandResult help(ParameterizedContext context)
    {
        return new PaginatedResult(context, new CustomCommandIterator());
    }

    private class CustomCommandIterator implements PaginationIterator
    {

        @Override
        public List<String> getPage(int page, int numberOfLines)
        {
            int counter = 0;
            int commandsSize = config.commands.size();
            int offset = page * numberOfLines;

            ArrayList<String> lines = new ArrayList<>();

            if (offset < commandsSize)
            {
                int lastItem = Math.min(offset + numberOfLines, commandsSize);

                for (Entry<String, String> entry : config.commands.entrySet())
                {
                    if (counter < offset)
                    {
                        counter++;
                        continue;
                    }
                    else if (counter > lastItem)
                    {
                        return lines;
                    }

                    lines.add("!" + entry.getKey() + " -> " + entry.getValue());
                }
            }
            return lines;
        }

        @Override
        public int pageCount(int numberOfLinesPerPage)
        {
            return (int) Math.ceil((float) config.commands.size() / (float) numberOfLinesPerPage);
        }
    }

    public static class CustomCommandCompleter implements Completer
    {
        @Override
        public List<String> complete(ParameterizedTabContext context, String token)
        {
            ArrayList<String> list = new ArrayList<>();
            for (String item : ((Customcommands)context.getCommand().getModule()).getConfig().commands.keySet())
            {
                if (item.startsWith(token.toLowerCase(ENGLISH)))
                {
                    list.add(item);
                }
            }
            Collections.sort(list);
            return list;
        }
    }
}
