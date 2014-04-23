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

import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;

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

    @Command(desc = "Adds a custom chat command.",
             indexed = {@Grouped(@Indexed("name")),
                        @Grouped(value = @Indexed("message"), greedy = true)},
             flags = @Flag(name = "force"))
    public void add(ParameterizedContext context)
    {
        String name = context.getString(0);
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

    @Command(desc = "Deletes a custom chat command.",
             indexed = @Grouped(@Indexed("name")))
    public void delete(ParameterizedContext context)
    {
        String name = context.getString(0);

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
}
